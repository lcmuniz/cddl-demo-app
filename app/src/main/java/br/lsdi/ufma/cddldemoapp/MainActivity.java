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

import android.hardware.Sensor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import br.ufma.lsdi.cddl.CDDL;
import br.ufma.lsdi.cddl.Connection;
import br.ufma.lsdi.cddl.ConnectionFactory;
import br.ufma.lsdi.cddl.message.Message;
import br.ufma.lsdi.cddl.pubsub.Subscriber;
import br.ufma.lsdi.cddl.pubsub.SubscriberFactory;


public class MainActivity extends AppCompatActivity {

    private Spinner spinner;
    private List<String> spinnerSensors;
    private ArrayAdapter<String> spinnerAdapter;

    private ListView listView;
    private List<String> listViewMessages;
    private ListViewAdapter listViewAdapter;
    private EditText filterEditText;

    private CDDL cddl;
    private String email = "lcmuniz@lsdi.ufma.br";
    private List<String> sensorNames;
    private String currentSensor;
    private Subscriber subscriber;

    private boolean filtering;

    private Handler handler = new Handler();

    EventBus eb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        eb = EventBus.builder().build();
        eb.register(this);

        if (savedInstanceState == null) {
            configCDDL();
        }

        configSpinner();
        configListView();
        configStartButton();
        configStopButton();
        configClearButton();
        configFilterButton();
    }

    private void configCDDL() {

        String host = CDDL.startMicroBroker();

        Connection connection = ConnectionFactory.createConnection();
        connection.setHost(host);
        connection.setClientId(email);
        connection.connect();

        cddl = CDDL.getInstance();
        cddl.setConnection(connection);
        cddl.setContext(this);

        cddl.startService();
        cddl.startCommunicationTechnology(CDDL.INTERNAL_TECHNOLOGY_ID);

        subscriber = SubscriberFactory.createSubscriber();
        subscriber.addConnection(cddl.getConnection());
        subscriber.setSubscriberListener(this::onMessage);

    }

    private void onMessage(Message message) {

        handler.post(() -> {
            Object[] valor = message.getServiceValue();
            listViewMessages.add(0, StringUtils.join(valor, ", "));
            listViewAdapter.notifyDataSetChanged();
        });

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void on(MessageEvent event) {
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
        appMenu.setMenuItem(MainActivity.this, item);
        return super.onOptionsItemSelected(item);
    }

    private void configSpinner() {

        List<Sensor> sensors = cddl.getInternalSensorList();
        sensorNames = sensors.stream().map(Sensor::getName).collect(Collectors.toList());

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, sensorNames);
        spinner = findViewById(R.id.spinner);
        spinner.setAdapter(adapter);

    }

    private void configListView() {
        listView = findViewById(R.id.listview);
        listViewMessages = new ArrayList<>();
        listViewAdapter = new ListViewAdapter(this, listViewMessages);
        listView.setAdapter(listViewAdapter);
    }

    private void configStartButton() {

        Button button = findViewById(R.id.start_button);
        button.setOnClickListener(e -> {
            stopCurrentSensor();
            startSelectedSensor();
        });

    }

    private void startSelectedSensor() {

        String selectedSensor = spinner.getSelectedItem().toString();
        cddl.startSensor(selectedSensor);

        subscriber.subscribeServiceByName(selectedSensor);
        currentSensor = selectedSensor;

        cddl.startLocationSensor();
    }

    private void stopCurrentSensor() {
        if (currentSensor != null) {
            cddl.stopSensor(currentSensor);
        }
    }

    private void configStopButton() {
        Button button = findViewById(R.id.stop_button);
        button.setOnClickListener(e -> stopCurrentSensor());
    }

    private void configClearButton() {
        final Button clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(e -> {
            listViewMessages.clear();
            listViewAdapter.notifyDataSetChanged();
        });
    }

    private void configFilterButton() {

        filterEditText = findViewById(R.id.filter_edittext);

        Button button = findViewById(R.id.filter_button);
        button.setOnClickListener(e -> {
            if (filterEditText.getText().toString().equals(""))
                return;

            if (filtering) {
                subscriber.clearFilter();
                button.setText(R.string.filter_button_label);
            }
            else {
                subscriber.setFilter(filterEditText.getText().toString());
                button.setText(R.string.clear_filter_button_label);
            }
            filtering = !filtering;

        });

    }

}
