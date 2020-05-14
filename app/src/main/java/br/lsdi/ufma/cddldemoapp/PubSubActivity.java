/**
 * Copyright 2019 LSDi - Laboratório de Sistemas Distribuídos Inteligentes
 * Universidade Federal do Maranhão
 *
 * This file is part of CDDLDemoApp.
 *
 * CDDLDemoApp is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Foobar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Foobar.  If not, see <https://www.gnu.org/licenses/>6.
 */

package br.lsdi.ufma.cddldemoapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.listeners.IMonitorListener;
import br.ufma.lsdi.cddl.message.Message;
import br.ufma.lsdi.cddl.pubsub.Monitor;
import br.ufma.lsdi.cddl.pubsub.MonitorImpl;
import br.ufma.lsdi.cddl.pubsub.Publisher;
import br.ufma.lsdi.cddl.pubsub.PublisherFactory;
import br.ufma.lsdi.cddl.pubsub.Subscriber;
import br.ufma.lsdi.cddl.pubsub.SubscriberFactory;

public class PubSubActivity extends AppCompatActivity {

    private static final String MY_SERVICE = "my-service";
    private EditText mensagemEditText;
    private TextView mensagensTextView;
    private CDDL cddl;
    private Publisher pub;
    private Subscriber sub;
    private EventBus eb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pub_sub);

        mensagemEditText = findViewById(R.id.mensagemEditText);

        mensagensTextView = findViewById(R.id.mensagensTextView);

        Button publicarButton = findViewById(R.id.publicarButton);
        publicarButton.setOnClickListener(this::onClick);

        eb = EventBus.builder().build();
        eb.register(this);


        configCDDL();
        configPublisher();
        configSubscriber();

    }

    private void configPublisher() {
        pub = PublisherFactory.createPublisher();
        pub.addConnection(cddl.getConnection());
    }

    private void configSubscriber() {
        sub = SubscriberFactory.createSubscriber();
        sub.addConnection(cddl.getConnection());

        sub.subscribeServiceByName(MY_SERVICE);
        sub.setSubscriberListener(this::onMessage);

    }

    private void onMessage(Message message) {
        eb.post(new MessageEvent(message));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on(MessageEvent event) {
        Object[] valor = event.getMessage().getServiceValue();
        mensagensTextView.setText((String) valor[0]);
    }


    private void configCDDL() {
        cddl = CDDL.getInstance();
    }

    @Override
    protected void onDestroy() {
        eb.unregister(this);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        AppMenu appMenu = AppMenu.getInstance();
        appMenu.setMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AppMenu appMenu = AppMenu.getInstance();
        appMenu.setMenuItem(PubSubActivity.this, item);
        return super.onOptionsItemSelected(item);
    }

    private void onClick(View view) {
        Message msg = new Message();
        msg.setServiceName(MY_SERVICE);
        msg.setServiceValue(mensagemEditText.getText().toString());
        pub.publish(msg);
    }

}
