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

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class AppMenu {

    private static final int SUBSCRIBE_SENSOR_MENU_ID = Menu.FIRST;
    private static final int PUB_SUB_MESSAGE_MENU_ID = Menu.FIRST + 1;

    private static AppMenu appMenu;

    public void setMenu(Menu menu) {
        menu.add(android.view.Menu.NONE, SUBSCRIBE_SENSOR_MENU_ID, android.view.Menu.NONE, R.string.subscrever_sensores);
        menu.add(android.view.Menu.NONE, PUB_SUB_MESSAGE_MENU_ID, android.view.Menu.NONE, R.string.pub_sub_mensagem);
    }

    public static AppMenu getInstance() {
        if (appMenu == null) {
            appMenu = new AppMenu();
        }
        return appMenu;
    }

    public void setMenuItem(Context ctx, MenuItem item) {
        switch (item.getItemId()) {
            case SUBSCRIBE_SENSOR_MENU_ID:
                ctx.startActivity(new Intent(ctx, MainActivity.class));
                return;
            case PUB_SUB_MESSAGE_MENU_ID:
                ctx.startActivity(new Intent(ctx, PubSubActivity.class));
                return;
        }
    }
}
