package it.gadg.contagiapp.evento;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import it.gadg.contagiapp.MainActivity;
import it.gadg.contagiapp.R;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;

import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;

public class GestisciEvento extends AppCompatActivity {

    FirebaseFirestore db;
    TextView NomeEventoGestione;
    TextView RuoloEventoGestione;
    String idEvento;
    String titoloEvento;
    String luogoEvento;
    String dataEvento;
    String oraEvento;
    int flag=0; // variabile per il controllo del buon esito dell'eliminazione dell'evento


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestisci_evento);

        db = FirebaseFirestore.getInstance();

        Intent intent = getIntent();
        titoloEvento =intent.getStringExtra("NomeEvento");
        luogoEvento = intent.getStringExtra("LuogoEvento");
        dataEvento = intent.getStringExtra("DataEvento");
        oraEvento = intent.getStringExtra("OraEvento");

        NomeEventoGestione = findViewById(R.id.NomeEventoGestione);
        NomeEventoGestione.setText(titoloEvento);

        RuoloEventoGestione = findViewById(R.id.RuoloEventoGestione);
        RuoloEventoGestione.setText("sei admin");

        idEvento = intent.getStringExtra("idEvento");

    }

    public void richiesteEvento(View view) {
        Intent intent = new Intent(getApplicationContext(), RichiesteEvento.class);
        intent.putExtra("idEvento",idEvento);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
    }

    public void aggEventoCalendario(View view) {


        String[] data = dataEvento.split("/");
        Calendar beginTime = Calendar.getInstance();
        String[] ora = oraEvento.split(":");

        beginTime.set(Integer.parseInt(data[2]), Integer.parseInt(data[1])-1, Integer.parseInt(data[0]), Integer.parseInt(ora[0]),  Integer.parseInt(ora[1]));
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, titoloEvento)
                .putExtra(CalendarContract.Events.EVENT_LOCATION, luogoEvento);
        startActivity(intent);

    }

    public void eliminaEvento(View view) {


        db.collection("PartecipazioneEvento").whereEqualTo("idEvento", idEvento).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        db.collection("PartecipazioneEvento").document(document.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    flag=1;
                                }
                            }
                        });
                }
            }
        });

        if(flag==0){
            db.collection("Eventi").document(idEvento).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        Toast.makeText(getApplicationContext(), "Evento eliminato correttamente", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Errore , riprova più tardi", Toast.LENGTH_LONG).show();
                    }

                }
            });
        }else{
            Toast.makeText(getApplicationContext(), "Errore , riprova più tardi", Toast.LENGTH_LONG).show();
        }



    }



    public void condividiEvento(View view) {

        String messaggio = "Partecipa adesso a " + titoloEvento +"\n\nCodice per inviare la richiesta : "+ idEvento;
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, messaggio);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);

    }
}