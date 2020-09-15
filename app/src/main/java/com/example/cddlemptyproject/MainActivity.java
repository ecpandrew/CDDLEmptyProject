package com.example.cddlemptyproject;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;



import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.ConnectionFactory;
import br.ufma.lsdi.cddl.listeners.IConnectionListener;
import br.ufma.lsdi.cddl.listeners.ISubscriberListener;
import br.ufma.lsdi.cddl.message.Message;
import br.ufma.lsdi.cddl.network.ConnectionImpl;
import br.ufma.lsdi.cddl.pubsub.Publisher;
import br.ufma.lsdi.cddl.pubsub.PublisherFactory;
import br.ufma.lsdi.cddl.pubsub.Subscriber;
import br.ufma.lsdi.cddl.pubsub.SubscriberFactory;

public class MainActivity extends Activity {

    CDDL cddl;

    private TextView messageTextView;
    private View sendButton;
    private ConnectionImpl conLocal;
    private ConnectionImpl conRemota;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setPermissions();

        setViews();

//        configConLocal();
//        configConRemota();
//        initCDDL();
//        subscribeMessage();
//
//        sendButton.setOnClickListener(clickListener);

    }


    private void configConLocal() {
//        val host = CDDL.startMicroBroker();
        String host = CDDL.startSecureMicroBroker(this, true);
        conLocal = ConnectionFactory.createConnection();
        conLocal.setClientId("mobile");
        conLocal.setHost(host);
        conLocal.addConnectionListener(connectionListener);
        conLocal.secureConnect(this);
    }
    private void configConRemota() {
        conRemota = ConnectionFactory.createConnection();
        conRemota.setClientId("mobile");
        conRemota.setHost("192.168.15.115");
        conRemota.addConnectionListener(connectionListener);
        conRemota.secureConnect(this);
    }


    private void initCDDL() {
        cddl = CDDL.getInstance();
        cddl.setConnection(conLocal);
        cddl.setContext(this);
        cddl.startService();
    }

    @Override
    protected void onDestroy() {
        cddl.stopLocationSensor();
        cddl.stopAllCommunicationTechnologies();
        cddl.stopService();
        conLocal.disconnect();
        CDDL.stopMicroBroker();
        super.onDestroy();
    }

    private void subscribeMessage() {
        Subscriber sub = SubscriberFactory.createSubscriber();
        sub.addConnection(conLocal);
        sub.subscribeServiceByName("SensorXYZ");
        sub.setSubscriberListener(new ISubscriberListener() {
            @Override
            public void onMessageArrived(Message message) {

                if (message.getServiceName().equals("SensorXYZ")) {
                    Log.d("_MAIN", "LOCAL: " + message);
                    publishMessageRemota();
                }else{
                    Log.d("_MAIN other", message.toString());
                }
            }
        });

    }

    private void publishMessageLocal() {

        Publisher publisher = PublisherFactory.createPublisher();
        publisher.addConnection(conLocal);

        Message message = new Message();

        message.setServiceName("SensorXYZ");
        message.setServiceByteArray("Valor");
        publisher.publish(message);
    }

    private void publishMessageRemota() {

        Publisher publisher = PublisherFactory.createPublisher();
        publisher.addConnection(conRemota);

        Message message = new Message();

        message.setServiceName("SensorXYZ");
        message.setServiceByteArray("Valor");
        publisher.publish(message);
    }




    private void setViews() {
        sendButton = findViewById(R.id.sendButton);
        messageTextView = (TextView) findViewById(R.id.messageTexView);
    }

    private View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            publishMessageLocal();
        }
    };

    private IConnectionListener connectionListener = new IConnectionListener() {
        @Override
        public void onConnectionEstablished() {
            messageTextView.setText("Conexão estabelecida.");
        }

        @Override
        public void onConnectionEstablishmentFailed() {
            messageTextView.setText("Falha na conexão.");
        }

        @Override
        public void onConnectionLost() {
            messageTextView.setText("Conexão perdida.");
        }

        @Override
        public void onDisconnectedNormally() {
            messageTextView.setText("Uma disconexão normal ocorreu.");
        }

    };

    private void setPermissions() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

    }

}

