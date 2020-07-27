package com.example.cddlemptyproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest.permission;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy;

import java.io.FileNotFoundException;

import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.Connection;
import br.ufma.lsdi.cddl.ConnectionFactory;
import br.ufma.lsdi.cddl.listeners.IConnectionListener;
import br.ufma.lsdi.cddl.listeners.ISubscriberListener;
import br.ufma.lsdi.cddl.message.Message;
import br.ufma.lsdi.cddl.message.ObjectFoundMessage;
import br.ufma.lsdi.cddl.network.SecurityService;
import br.ufma.lsdi.cddl.pubsub.Subscriber;
import br.ufma.lsdi.cddl.pubsub.SubscriberFactory;

public class MainActivity extends AppCompatActivity {



    private Connection localConn;
    private Connection secureLocalConn;
    private CDDL cddl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT > 9) {
            ThreadPolicy policy = new ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        setPermissions();

//        SecurityService securityService = CDDL.getSecurityServiceInstance(getApplicationContext());
//
//        securityService.grantReadPermissionByCDDLTopic("cardoso", SecurityService.ALL_TOPICS);
//        securityService.grantWritePermissionByCDDLTopic("cardoso", SecurityService.ALL_TOPICS);

//        securityService.revokeReadPermissionByCDDLTopic("cardoso", SecurityService.OBJECT_FOUND_TOPIC);

//        securityService.grantReadPermissionByServiceName("cardoso","MeuServico");
//        securityService.grantReadPermissionByCustomTopic("cardoso","teste/sensor/2");

//        securityService.revokeReadPermissionByCDDLTopic("cardoso", SecurityService.OBJECT_FOUND_TOPIC);
//        securityService.revokeReadPermissionByServiceName("cardoso","MeuServico");
//        securityService.revokeReadPermissionByCustomTopic("cardoso","/teste/sensor/2");


        /////////////////////////////////////////////////////////
        // Configurando os certificados:
        /////////////////////////////////////////////////////////
        // 1 - gerar o CSR (certificate signing request)
        // Esse método vai gerar uma chave privada e utilizando ela vai gerar o CSR do usúario, o CSR é baixado para pasta "Download" no smartphone
        //
//             securityService.generateCSR("João da Silva","UFMA","LSDi","São Luis","MA", "Brasil");
        //
        //
        // 2 - assinando o certificado
        //     Essa parte é feita fora da aplicação. Nesse link tem um guia de como gerar um certificado para servir como autoridade certificadora.
        //     https://gist.github.com/fntlnz/cf14feb5a46b2eda428e000157447309
        //
        //     2.1 - Utilizando o guia a cima devemos criar um certificado digital para atuar como autoridade certificadora (CA)
        //     2.2 - Utilizando o guia a cima devemos assinar o CSR (que se encontra na pasta Downloads), gerando assim o certificado digital do usuário assinado pela CA.
        //     2.3 - Após gerar o certificado, devemos colocar na pasta Download os seguintes arquivos: Certificado Assinado do usuário e o Certificado da CA.
        //
        /////////////////////////////////////////////////////////
        // Importando os certificados
        /////////////////////////////////////////////////////////
        //  1 - Após colocar os 2 certificados na pasta download devemos apenas importa-los através dos metodos
//        try {
//            securityService.setCaCertificate("rootCA.crt");
//            securityService.setCertificate("client.crt");
//
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        /////////////////////////////////////////////////////////

        // Obs: Esse processo precisa ser feito apenas uma vez.
        // Ao utilizar a conexão segura ou o broker seguro, os certificados e chaves serão automaticamente ofertados para criação do contexto SSL.
        // Dessa forma, o broker no modo seguro apenas aceita clientes que possuem um certificado assinado pela CA que ele confia (aquela que foi importada).
        // Analogamente, o cliente também não se conecta quando o certificado do broker não é assinado pela CA que ele confia.

        ///////////////////////////////////////////////////////
        // Adicionando permissões de controle de acesso.
        ///////////////////////////////////////////////////////
        //      securityService.grantReadPermissionByCDDLTopic("clientID", OBJECT_FOUND_TOPIC);
        //      securityService.grantWritePermissionByCDDLTopic("clientID", OBJECT_FOUND_TOPIC);
        ///////////////////////////////////////////////////////

        ////////////////////////////////////////////////////////
        // Utilizando o Modo seguro
        ///////////////////////////////////////////////////////

        // Modo normal:
//            configLocalConn();
//            configCDDL(getApplicationContext());
//            initSubscriber(localConn);

        // Modo seguro:
            configSecureLocanConn(getApplicationContext());
            configSecureCDDL(getApplicationContext());
            initSubscriber(secureLocalConn);

    }




    private void configLocalConn(){
        String host = CDDL.startMicroBroker();
        localConn = ConnectionFactory.createConnection();
        localConn.setClientId("cardoso");
        localConn.setHost(host);
        localConn.addConnectionListener(connectionListener);
        localConn.connect();
    }
    private void configSecureLocanConn(Context context){
        String host = CDDL.startSecureMicroBroker(context, true);
        secureLocalConn = ConnectionFactory.createConnection();
        secureLocalConn.setClientId("cardoso");
        secureLocalConn.setHost(host);
        secureLocalConn.addConnectionListener(secureConnectionListener);
        secureLocalConn.secureConnect(context);
    }

    private void configCDDL(Context context){
        try {
            cddl = CDDL.getInstance();
            if (cddl != null) {
                cddl.setConnection(localConn);
                cddl.setContext(context);
                cddl.startService();
                cddl.startCommunicationTechnology(CDDL.BLE_TECHNOLOGY_ID);
                System.out.println("-----------CDDL-----------");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    private void configSecureCDDL(Context context){
        try {
            cddl = CDDL.getInstance();
            if (cddl != null) {
                cddl.setConnection(secureLocalConn);
                cddl.setContext(context);
                cddl.startService();
                cddl.startCommunicationTechnology(CDDL.BLE_TECHNOLOGY_ID);
                System.out.println("----------- CDDL Modo Seguro -----------");
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }


    private void initSubscriber(Connection conn){
        Subscriber subscriber = SubscriberFactory.createSubscriber();
        subscriber.addConnection(conn);
        subscriber.subscribeObjectFoundTopic();
        subscriber.setSubscriberListener(new ISubscriberListener() {
            @Override
            public void onMessageArrived(Message message) {
                if(message instanceof ObjectFoundMessage){
                    System.out.println("Recebido >>>>> " + message);
                }
            }
        });

    }









    private IConnectionListener connectionListener = new IConnectionListener() {
        @Override
        public void onConnectionEstablished() {
            System.out.println("Conexão local estabelecida");
        }

        @Override
        public void onConnectionEstablishmentFailed() {
            System.out.println("Conexão local falhou");

        }

        @Override
        public void onConnectionLost() {
            System.out.println("Conexão local perdida");

        }

        @Override
        public void onDisconnectedNormally() {
            System.out.println("Conexão local foi desconectada normalmente");

        }
    };
    private IConnectionListener secureConnectionListener = new IConnectionListener() {
        @Override
        public void onConnectionEstablished() {
            System.out.println("Conexão segura estabelecida");
        }

        @Override
        public void onConnectionEstablishmentFailed() {
            System.out.println("Conexão segura falhou");

        }

        @Override
        public void onConnectionLost() {
            System.out.println("Conexão segura perdida");

        }

        @Override
        public void onDisconnectedNormally() {
            System.out.println("Conexão segura foi desconectada normalmente");

        }
    };
    private void setPermissions(){
        if (ActivityCompat.checkSelfPermission(this,
                permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{permission.ACCESS_FINE_LOCATION,
                            permission.WRITE_EXTERNAL_STORAGE, permission.ACCESS_COARSE_LOCATION,
                            permission.ACCESS_BACKGROUND_LOCATION, permission.WAKE_LOCK}, 1
            );
        }
    }

}
