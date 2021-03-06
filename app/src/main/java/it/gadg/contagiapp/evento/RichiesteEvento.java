package it.gadg.contagiapp.evento;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import it.gadg.contagiapp.R;

import it.gadg.contagiapp.modelli.UtenteRichiesta;


public class RichiesteEvento extends AppCompatActivity {

    ListView listView;
    FirebaseFirestore db;
    int flag;
    String[] idRichiesteUtenti;
    String[] nomi;
    String[] rischi;
    ArrayList<UtenteRichiesta> ur = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_richieste_evento);

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));


        Intent intent = getIntent();
        String id =intent.getStringExtra("idEvento"); //ricavo l'id dell'evento in questione

        db = FirebaseFirestore.getInstance();
        //estraggo tutte le partecipazioni con contengono l'id dell'evento
        db.collection("PartecipazioneEvento").whereEqualTo("idEvento", id).whereEqualTo("status", 0).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                flag = queryDocumentSnapshots.size();

                //se non ci sono cambio layout
                if (flag==0){
                    setContentView(R.layout.no_richieste_eventi);

                }else {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                        String UID = document.getString("UID");
                        final UtenteRichiesta x = new UtenteRichiesta(UID);//creo un oggetto specifico per la richiesta
                        x.idRichiesta = document.getId();

                        //ricavo le informazioni dell'utente che ha fatto richiesta
                        db.collection("Utenti").document(UID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document != null) {
                                        x.nome=document.getString("nome");
                                        x.cognome=document.getString("cognome");
                                        x.rischio= (long) document.get("rischio");

                                        salvaUtente(x);//confermo l'utente

                                    }
                                } else {
                                    Log.d("Errore", "get failed with ", task.getException());
                                }
                            }

                        });
                    }
                }

            }


            private void salvaUtente(UtenteRichiesta x) {
                ur.add(x);//aggiungo la richiesta alla lista delle richieste

                //controllo se ho memorizzato tutte le richieste
                if (ur.size() == flag) {
                    idRichiesteUtenti = new String[ur.size()];
                    for (int j = 0; j < ur.size(); j++) {
                        idRichiesteUtenti[j] = ur.get(j).idRichiesta;
                    }

                    nomi = new String[ur.size()];
                    for (int j = 0; j < ur.size(); j++) {
                        nomi[j] = ur.get(j).nome + " " +ur.get(j).cognome;

                    }

                    rischi = new String[ur.size()];
                    for (int j = 0; j < ur.size(); j++) {
                        rischi[j] = String.valueOf(ur.get(j).rischio);
                    }

                    listView = findViewById(R.id.listaRichieste);
                    RichiesteEvento.Adapter adapter = new RichiesteEvento.Adapter(getApplicationContext(), nomi, idRichiesteUtenti,rischi);
                    listView.setAdapter(adapter);
                }

            }
        });


    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }


    class Adapter extends ArrayAdapter<String> {
        Context context;
        String NomiUtenti[];
        String idRichiesteUtenti[];
        String Rischi[];

        Adapter (Context c,String NomiUtenti[] , String idRichiesteUtenti[],String Rischi[]){
            super(c,R.layout.rigarichiesta,R.id.NomeUtente,NomiUtenti);
            this.context = c;
            this.NomiUtenti=NomiUtenti;
            this.idRichiesteUtenti=idRichiesteUtenti;
            this.Rischi=Rischi;


        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            LayoutInflater layoutInflater = (LayoutInflater)getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View rigarichiesta = layoutInflater.inflate(R.layout.rigarichiesta,parent,false);
            TextView NomeUtente = rigarichiesta.findViewById(R.id.NomeUtente);
            TextView idRichiesta = rigarichiesta.findViewById(R.id.idRichiestaPartecipazione);
            TextView Rischio = rigarichiesta.findViewById(R.id.RischioUtente);
            if(Integer.parseInt(rischi[position])>50){
                Rischio.setTextColor(Color.RED);
            }else{
                Rischio.setTextColor(Color.rgb(110,139,37));
            }

            NomeUtente.setText(NomiUtenti[position]);
            idRichiesta.setText(idRichiesteUtenti[position]);
            final String temp = "Rischo positività : " + rischi[position] +"%";
            Rischio.setText(temp);

            final Button accetta = rigarichiesta.findViewById(R.id.accettaRichiesta);
            accetta.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               accettaInvito(idRichiesteUtenti[position], Integer.parseInt(rischi[position]));

                                           }

                                           private void accettaInvito(final String id, final int rischio) {

                                               //controllo che il rischio sia sotto la soglia accettabile
                                               if(rischio<50){
                                                   //aggiorno lo status della richiesta
                                                   db.collection("PartecipazioneEvento").document(id).update("status", 1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                       @Override
                                                       public void onComplete(@NonNull Task<Void> task) {
                                                           recreate();
                                                           if(task.isSuccessful()){
                                                               //prendo l'id dell'evento
                                                               db.collection("PartecipazioneEvento").document(id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                   @Override
                                                                   public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                       DocumentSnapshot document1 = task.getResult();
                                                                       //prendo il docuemnto dell'evento
                                                                       db.collection("Eventi").document((String) document1.get("idEvento")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                           @Override
                                                                           public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                               DocumentSnapshot document2 = task.getResult();
                                                                                //controllo il rischio dell'utnete , se è maggiore di quello dell'evento aggiorno il rischio dell'evento
                                                                               if(rischio>(long)document2.get("rischio")){
                                                                                   //aggiorno il rischio dell'evento
                                                                                   db.collection("Eventi").document(document2.getId()).update("rischio", rischio).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                                       @Override
                                                                                       public void onComplete(@NonNull Task<Void> task) {
                                                                                           Toast.makeText(getApplicationContext(), getResources().getString(R.string.reqAcc), Toast.LENGTH_LONG).show();
                                                                                       }
                                                                                   });
                                                                               }
                                                                           }
                                                                       });

                                                                   }
                                                               });

                                                           }else{
                                                               Toast.makeText(getApplicationContext(), getResources().getString(R.string.err), Toast.LENGTH_LONG).show();
                                                           }

                                                       }
                                                   });
                                               }else{
                                                   Toast.makeText(getApplicationContext(),  getResources().getString(R.string.tooHighRiskReq), Toast.LENGTH_LONG).show();
                                               }
                                                     



                                           }
                                       }
            );

            final Button rifiuta = rigarichiesta.findViewById(R.id.rifiutaRichiesta);
            rifiuta.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               rifiutaInvito(idRichiesteUtenti[position]);
                                           }

                                           private void rifiutaInvito(String id) {


                                                           db.collection("PartecipazioneEvento").document(id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                   recreate();
                                                                   if(task.isSuccessful()){
                                                                       Toast.makeText(getApplicationContext(),  getResources().getString(R.string.reqDef), Toast.LENGTH_LONG).show();
                                                                   }else{
                                                                       Toast.makeText(getApplicationContext(),  getResources().getString(R.string.err), Toast.LENGTH_LONG).show();
                                                                   }

                                                               }
                                                           });


                                           }
                                       }
            );

            return rigarichiesta;
        }
    }


}