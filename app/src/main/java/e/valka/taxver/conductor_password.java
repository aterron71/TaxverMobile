package e.valka.taxver;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONObject;

import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import e.valka.taxver.Models.Conductor;
import e.valka.taxver.Models.Usuarios;
import e.valka.taxver.Utils.DownloadAsyncTask;
import e.valka.taxver.Utils.URLS;

import static e.valka.taxver.Login.KEY_DEVICEID;
import static e.valka.taxver.Login.KEY_EMAIL;
import static e.valka.taxver.Login.KEY_PASSWORD;

public class conductor_password extends AppCompatActivity {
    Conductor con;
    String deviceID = null;
    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conductor_password);
        TextView nombre = findViewById(R.id.bienvenido);
        Button iniciar = findViewById(R.id.iniciarsesion2);
        EditText pass1 = findViewById(R.id.pass1);
        EditText pass2 = findViewById(R.id.pass2);
        con = (Conductor)getIntent().getSerializableExtra("conductor");
        deviceID = getIntent().getStringExtra("phoneID");
        nombre.setText("¡Bienvenide "+ con.IdPersonaNavigation.Nombre+"!\nIngresa tu nueva contraseña.");
        sharedPreferences = getSharedPreferences (Login.MY_PREFERENCES, Context.MODE_PRIVATE);
        iniciar.setOnClickListener((v)->{
            if(!pass1.getText().toString().isEmpty() && !pass2.getText().toString().isEmpty()){
                if(pass1.getText().toString().equals(pass2.getText().toString())){
                    Usuarios usuario = new Usuarios();
                    usuario.Nombre = con.IdPersonaNavigation.Email;
                    usuario.Password = pass1.getText().toString();
                    usuario.IdPersona = con.IdPersona;
                    usuario.IdPersonaNavigation = con.IdPersonaNavigation;
                    usuario.PhoneId = deviceID;
                    sendPost(usuario);
                }else{
                    Toast.makeText (getBaseContext (), "¡Contraseñas diferentes!", Toast.LENGTH_LONG).show ();
                }
            }else{
                Toast.makeText (getBaseContext (), "¡Escribre tu contraseña!", Toast.LENGTH_LONG).show ();
            }
        });
    }
    public void sendPost(Usuarios usuario) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(URLS.CreateConductor);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                    conn.setRequestProperty("Accept","application/json");
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    JSONObject jsonParam = new JSONObject(new Gson().toJson(usuario));

                    Log.i("JSON", jsonParam.toString());
                    DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                    //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                    os.writeBytes(jsonParam.toString());

                    os.flush();
                    os.close();

                    Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                    Log.i("MSG" , conn.getResponseMessage());

                    conn.disconnect();
                    new DownloadAsyncTask(s ->{
                        Usuarios usuarioN = parseJSON(s);
                        SharedPreferences.Editor editor = sharedPreferences.edit ();
                        editor.putString(KEY_EMAIL,usuario.Nombre);
                        editor.putString(KEY_PASSWORD,usuario.Password);
                        editor.putString(KEY_DEVICEID,usuario.PhoneId);
                        editor.apply();
                        if(usuarioN != null){
                            Intent iniciarsesion = new Intent(getBaseContext(),navigationActivity.class);
                            iniciarsesion.putExtra("usuario",usuarioN);
                            startActivity(iniciarsesion);
                        }}).execute (URLS.LoginApp+"?Correo="+usuario.IdPersonaNavigation.Email+"&password="+usuario.Password+"&phoneID="+usuario.PhoneId);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }
    private Usuarios parseJSON (String json) {
        Usuarios usuario = new Gson().fromJson (json, Usuarios.class);
        if (usuario == null) return null;
        return usuario;
    }
}
