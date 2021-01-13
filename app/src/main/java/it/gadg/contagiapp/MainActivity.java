package it.gadg.contagiapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import it.gadg.contagiapp.evento.CreaEventoActivity;
import it.gadg.contagiapp.evento.InviaPartecipazioneEvento;
import it.gadg.contagiapp.evento.ListaEventiActivity;
import it.gadg.contagiapp.gruppo.CreaGruppoActivity;
import it.gadg.contagiapp.gruppo.InvitiGruppi;
import it.gadg.contagiapp.gruppo.ListaGruppiActivity;
import it.gadg.contagiapp.splash.Splash;


public class MainActivity extends Activity {

    private FirebaseAuth mAuth; //dichiaro variabile per l'auenticazione firebase

    private AlertDialog.Builder dialogBuilder;
    private AlertDialog dialog;
    private TextView richiestaPopup;
    private Button siInAttesaPopup, noInAttesaPopup;

    public String id;

    FirebaseFirestore db;

    Button positivoB;
    Button negativoB;
    Button inAttesaB;

    private int RISCHIO_POSITIVO=50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAuth= FirebaseAuth.getInstance();
        FirebaseUser u = mAuth.getCurrentUser();
        id = u.getUid();

        positivoB = findViewById(R.id.positivoB);
        negativoB = findViewById(R.id.negativoB);
        inAttesaB = findViewById(R.id.inAttesaB);

        db = FirebaseFirestore.getInstance();


        db.collection("Utenti")
                .document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {

                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot document = task.getResult();
                        String etichetta = (String) document.get("etichetta");
                        if (etichetta.equals("super")||etichetta.equals("sicuro")||etichetta.equals("incerto")){
                            inAttesaB.setVisibility(View.VISIBLE);
                        }
                              else if (etichetta.equals("test")){

                                  positivoB.setVisibility(View.VISIBLE);
                                  negativoB.setVisibility(View.VISIBLE);
                        }

                                else if (etichetta.equals("positivo")){

                                    inAttesaB.setVisibility(View.VISIBLE);
                                    }

                    }
                });
    }

    public void logout(View view) {
        mAuth.signOut();
        if (null == FirebaseAuth.getInstance().getCurrentUser()) {
            Toast.makeText(getApplicationContext(), "Logout riuscito.",
                    Toast.LENGTH_SHORT).show();
            Intent i = new Intent(getApplicationContext(), Splash.class);
            startActivity(i);
        } else {
            Toast.makeText(getApplicationContext(), "Logout fallito.",
                    Toast.LENGTH_SHORT).show();
        }
    }


    public void creaEvento(View view) {
        Intent i = new Intent(getApplicationContext(), CreaEventoActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);

    }

    public void creaGruppo(View view) {
        Intent i = new Intent(getApplicationContext(), CreaGruppoActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    public void ListaEventi(View view) {
        Intent i = new Intent(getApplicationContext(), ListaEventiActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    public void ListaGruppi(View view) {
        Intent i = new Intent(getApplicationContext(), ListaGruppiActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    public void ListaInviti(View view) {
        Intent i = new Intent(getApplicationContext(), InvitiGruppi.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    public void cercaEvento(View view) {
        Intent i = new Intent(getApplicationContext(), InviaPartecipazioneEvento.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    public void registraContatto(View view) {
        //TODO scambiare informazioni tramite bluetooth ogetto(UID,rischio,data)
        Intent i = new Intent(getApplicationContext(), BtActivity.class);
        startActivity(i);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    public void createNewContactDialog(final int i) {


        dialogBuilder = new AlertDialog.Builder(this);
        final View contactPopupView  = getLayoutInflater().inflate(R.layout.popupmain, null);
        richiestaPopup =  contactPopupView.findViewById(R.id.richiestaPopup);

        if(i == 1){
            String temp = "Confermi di essere positivo?";
            richiestaPopup.setText(temp);
        }
           else if (i == 2){
               String temp = "Confermi di essere in Attesa?";
               richiestaPopup.setText(temp);
           }
              else if (i == 3){
                 String temp = "Confermi di essere in Negativo?";
                 richiestaPopup.setText(temp);

              }

        siInAttesaPopup =  contactPopupView.findViewById(R.id.siInAttesaPopup);
        noInAttesaPopup =  contactPopupView.findViewById(R.id.noInAttesaPopup);

        dialogBuilder.setView(contactPopupView);
        dialog = dialogBuilder.create();
        dialog.show();

        siInAttesaPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(i==1){
                   //TODO Modifica nel db l'etichetta in "positivo" trovo le etivhette sul file del drive

                    db.collection("Utenti").document(id).update("etichetta", "positivo").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            recreate();
                            if(task.isSuccessful()){
                                db.collection("Utenti").document(id).update("rischio", RISCHIO_POSITIVO).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        recreate();
                                        if(task.isSuccessful()){
                                            Toast.makeText(getApplicationContext(), "Richiesta accettata correttamente", Toast.LENGTH_LONG).show();
                                            recreate();
                                        }else{
                                            Toast.makeText(getApplicationContext(), "Errore , riprova più tardi", Toast.LENGTH_LONG).show();
                                        }

                                    }
                                });
                            }else{
                                Toast.makeText(getApplicationContext(), "Errore , riprova più tardi", Toast.LENGTH_LONG).show();
                            }

                        }
                    });
                }else if(i==2){
                    //TODO Modifica nel db l'etichetta in "test" trovo le etivhette sul file del drive
                }else if(i==3){
                    //TODO Modifica nel db l'etichetta in "super" trovo le etivhette sul file del drive
                }

            }
        });

        noInAttesaPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });



    }

    public void popupPositivo(View view) {
         this.createNewContactDialog(1);
    }

    public void popupInAttesa(View view) {
        this.createNewContactDialog(2);
    }

    public void popupNegativo(View view) {
        this.createNewContactDialog(3);
    }
}
