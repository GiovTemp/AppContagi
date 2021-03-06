package it.gadg.contagiapp.evento;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;

import it.gadg.contagiapp.MainActivity;
import it.gadg.contagiapp.R;

public class VisualizzaEventoActivity extends AppCompatActivity {
    TextView NomeEventoVisualizza;
    String titoloEvento;
    String luogoEvento;
    String dataEvento;
    String oraEvento;
    String idEvento;
    TextView InfoGestione;
    private AlertDialog.Builder PopupAbbandonaEvento;
    private AlertDialog dialog;
    private CardView siAbbandonoE, noAbbandonoE;
    FirebaseFirestore db;
    private FirebaseAuth mAuth; //dichiaro variabile per l'auenticazione firebase
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualizza_evento);

        getWindow().setNavigationBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));


        db = FirebaseFirestore.getInstance();
        mAuth= FirebaseAuth.getInstance();
        Intent intent = getIntent();
        titoloEvento =intent.getStringExtra("NomeEvento");
        luogoEvento = intent.getStringExtra("LuogoEvento");
        dataEvento = intent.getStringExtra("DataEvento");
        oraEvento = intent.getStringExtra("OraEvento");
        idEvento = intent.getStringExtra("idEvento");

        NomeEventoVisualizza = findViewById(R.id.NomeEventoVisualizza);
        NomeEventoVisualizza.setText(titoloEvento);


        InfoGestione = findViewById(R.id.infoEvento);
        String temp=" il " + dataEvento +" dalle " + oraEvento + "\n\npresso : " +luogoEvento;
        InfoGestione.setText(temp);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
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

    public void abbandonaEvento() {
        db.collection("PartecipazioneEvento").whereEqualTo("idEvento", idEvento).whereEqualTo("UID",mAuth.getCurrentUser().getUid()).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {

            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    db.collection("PartecipazioneEvento").document(document.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.err), Toast.LENGTH_LONG).show();
                            }else{
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.eventAbb), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
    }



    public void condividiEvento(View view) {


        String messaggio = getResources().getString(R.string.partecipa) +" " + titoloEvento +"\n\n"+getResources().getString(R.string.codiceShare) +" "+ idEvento;
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, messaggio);
        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        startActivity(shareIntent);

    }
    public void ConfermaAbbandonoEvento(View view) {


        PopupAbbandonaEvento = new AlertDialog.Builder(this);
        final View contactPopupView = getLayoutInflater().inflate(R.layout.popupabbandonaevento, null);




        siAbbandonoE = contactPopupView.findViewById(R.id.siAbbandonoE);
        noAbbandonoE= contactPopupView.findViewById(R.id.noAbbandonoE);

        PopupAbbandonaEvento.setView(contactPopupView);
        dialog = PopupAbbandonaEvento.create();
        dialog.show();

        siAbbandonoE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abbandonaEvento();
                dialog.dismiss();
            }
        });

        noAbbandonoE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

    }

}