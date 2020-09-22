package br.lsdi.ufma.cddldemoapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import org.greenrobot.eventbus.Subscribe;

import java.util.UUID;

import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.Connection;
import br.ufma.lsdi.cddl.ConnectionFactory;
import br.ufma.lsdi.cddl.pubsub.Monitor;
import br.ufma.lsdi.cddl.pubsub.Subscriber;
import br.ufma.lsdi.cddl.pubsub.SubscriberFactory;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setPermissions();

        bootstrap();
    }

    private void bootstrap() {
        String host = "broker.mqttdashboard.com";

        Connection connection = ConnectionFactory.createConnection();
        connection.setHost(host);
        connection.setClientId(UUID.randomUUID().toString());
        connection.connect();

        CDDL cddl = CDDL.getInstance();
        cddl.setConnection(connection);
        cddl.setContext(this);

        cddl.startService();
        cddl.startCommunicationTechnology(CDDL.INTERNAL_TECHNOLOGY_ID);

        Subscriber subscriber = SubscriberFactory.createSubscriber();
        subscriber.addConnection(connection);
        subscriber.subscribeServiceByName("Goldfish Light sensor");
        subscriber.subscribeServiceByName("Goldfish 3-axis Accelerometer");

        cddl.startSensor("Goldfish Light sensor");
        cddl.startSensor("Goldfish 3-axis Accelerometer");

        subscriber.setSubscriberListener(message -> {
            System.out.println(">" + message);
            if (message.getServiceName().equals("Goldfish Light sensor")) {
                //System.out.println("Fazer algo com mensagens do sensor de iluminação");
                //System.out.println(message.getServiceValue());
            }
            else if (message.getServiceName().equals("Goldfish 3-axis Accelerometer")) {
                //System.out.println("Fazer algo com mensagens do acelerômetro");
                //System.out.println(message.getServiceValue());
            }
        });

        String epl = "select * from Message " +
                "match_recognize(" +
                    "measures A as m1, B as m2 " +
                    "pattern (A B) " +
                    "define " +
                    "A as A.serviceName = 'Goldfish Light sensor', " +
                    "B as B.serviceName = 'Goldfish Light sensor' and (cast(A.serviceValue[0], int) - cast(B.serviceValue[0], int))  > 1000" +
                ")";

        subscriber.getMonitor().addRule(epl, message -> {
            System.out.println("====================================");
            System.out.println(message.getServiceValue());
            System.out.println("====================================");
        });

    }

    private void setPermissions() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

}
