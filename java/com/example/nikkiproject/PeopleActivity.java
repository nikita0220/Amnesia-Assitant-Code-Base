package com.example.nikkiproject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PeopleActivity extends AppCompatActivity implements RecyclerAdapter.ItemClickListener{

    private ArrayList<Person> people = null;
    private DataManager dataManager = null;
    private RecyclerAdapter adapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_people);
        dataManager = new DataManager(getApplicationContext());
        dataManager.initializeDb();
        people = dataManager.getAll();

        RecyclerView recyclerView = findViewById(R.id.people_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecyclerAdapter(this, people);
        adapter.setClickListener(this);
        recyclerView.setAdapter(adapter);
        Log.v("NIKITA-saved-people-size", String.valueOf(people.size()));

    }

    private void updateView() {
        clearView();
        people = dataManager.getAll();
        adapter.notifyItemRangeChanged(0, people.size()-1);
    }

    public void clearView(){
        int size = people.size();
        people.clear();
        adapter.notifyItemRangeRemoved(0, size);
    }

    private void deleteFromLocal(Integer index, String personId) {
        dataManager.deletePerson(personId);
            //updateView();
            people.remove(index);
            adapter.notifyItemRemoved(index);
            //adapter.notifyItemRangeChanged(index, people.size());
            Toast.makeText(PeopleActivity.this, "User Deleted" + String.valueOf(index), Toast.LENGTH_SHORT).show();
    }

    private void deleteFromAzure(String personId, Integer index) {
        Log.v("NIKITA", "Delete azure");
        if (!isNetworkConnected()) {
            Toast.makeText(PeopleActivity.this, "Not connected to the internet", Toast.LENGTH_SHORT).show();
        }
        ReqModel req = new ReqModel("Delete", personId);
        API api = RetrofitClient.getInstance().getAPI();
        Call<ReqModel> deleteUser = api.delete(req);

        deleteUser.enqueue(new Callback<ReqModel>() {
            @Override
            public void onResponse(Call<ReqModel> call, Response<ReqModel> response) {
                if(response.isSuccessful()) {
                    deleteFromLocal(index, personId);
                    Log.v("NIKITA", "Deleted");
                }
            }
            @Override
            public void onFailure(Call<ReqModel> call, Throwable t) {
                Log.v("REST225", t.toString());
                Toast.makeText(PeopleActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class deleteFromServerAsync extends AsyncTask<Object, Void, Integer> {
        @Override
        protected Integer doInBackground(Object... params) {
            String id = (String) params[1];
            Integer index = (Integer) params[0];
            deleteFromAzure(id, index);
            return index;
        }
        @Override
        protected void onPostExecute(Integer index) {
            Log.v("NIKITA", "Deleted User");

        }
    }

    public void deletePerson(int index){
        String id = people.get(index).getId();
        new deleteFromServerAsync().execute(index, id);
    }

    @Override
    public void onItemClick(View view, int position) {
        new AlertDialog.Builder(PeopleActivity.this)
                .setCancelable(false)
                .setTitle("Remove Item")
                .setMessage("Do you want to remove person - " + people.get(position).getName() + " ?")
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deletePerson(position);
                    }
                }).create().show();
    }
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null;
    }
}