/**
 *	Copyright 2010-2012 Norio bvba
 *
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.norio.twunch.android.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import be.norio.twunch.android.ui.fragment.DetailsFragment;

public class DetailsActivity extends BaseActivity {

    private static final String EXTRA_ID = "EXTRA_ID";

    public static void start(Context context, String id) {
        Intent intent = getIntent(context, id);
        context.startActivity(intent);
    }

    public static Intent getIntent(Context context, String id) {
        Intent intent = new Intent(context, DetailsActivity.class);
        intent.putExtra(EXTRA_ID, id);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content, DetailsFragment.newInstance(getIntent().getStringExtra(EXTRA_ID))).commit();
    }

}
