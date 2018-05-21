package com.example.carlo.ubicatedam;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.carlo.ubicatedam.fragments.BienvenidaFragment;
import com.example.carlo.ubicatedam.interfaces.IFragments;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, GoogleApiClient.OnConnectionFailedListener, IFragments {

    private TextView txtNameUser, txtEmailUser, txtAuthUser;
    private ImageView imageUserFirebase;
    //private Button btnLogOut, btnRevoke;


    /*Login silencioso se hace en el onStart*/
    private GoogleApiClient googleApiClient;

    /*Firebase*/
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        /*ELEMENTOS DEL DRAWERLAYOUT*/

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        /*Instancio el header del menu lateral y le paso esa vista al método instanciarMiembros para
        * que pueda acceder a los elementos imagen, nombre, emai y idAuth*/
        View header=navigationView.getHeaderView(0);
        instanciarMiembros(header);

        //Ponemos el fragment de formulario por defecto
        Fragment fragment= new BienvenidaFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.content_main, fragment).commit();


        /*GOOGLE*/

        GoogleSignInOptions gso= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleApiClient= new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        /*FIREBASE*/

        firebaseAuth= FirebaseAuth.getInstance();
        firebaseListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //Obtener datos desde firebase
                FirebaseUser user= firebaseAuth.getCurrentUser();
                if(user != null){
                    setUserData(user);
                }else{
                    goLogInScreen();
                }

            }
        };



    }



    private void instanciarMiembros(View view) {

        txtNameUser= view.findViewById(R.id.idNameUserFirebase);
        txtEmailUser= view.findViewById(R.id.idEmailUserFirebase);
        txtAuthUser= view.findViewById(R.id.idAuthlUserFirebase);
        imageUserFirebase= view.findViewById(R.id.idImageUserFirebase);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_logout) {
            logout();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        /*Las diferentes partes del menu lateral
        * Aqui cargaremos el fragment que elijamos en la unica actividad de la aplicación*/
        int id = item.getItemId();

        Fragment miFragment=null;
        boolean fragmentSeleccionado=false;


        if (id == R.id.nav_inicio) {

            miFragment=new BienvenidaFragment();
            fragmentSeleccionado=true;

        } else if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        if (fragmentSeleccionado==true){
            getSupportFragmentManager().beginTransaction().replace(R.id.content_main, miFragment).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /*------------------------------------------------------------------------------------------*/


    /*Login silencioso
     *
     * onStart se encarga de verificar si estamos autenticados o no
     * */

    @Override
    protected void onStart() {
        super.onStart();

        firebaseAuth.addAuthStateListener(firebaseListener);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(firebaseListener != null){
            firebaseAuth.removeAuthStateListener(firebaseListener);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Si falla la conexión
    }


    /*Redirige a Login si no estas logeado*/
    private void goLogInScreen() {
        Intent intent= new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    ///////////////Métodos de botones///////////////
    /*Acciones de los botones
     * Estos llaman a sus métodos correspondientes
     * Ambos retornan un objetos status al cual devemos preguntar si esta operacion fue exitosa
     * */

    public void logout() {

        //Cerrar session cuando se presione el boton
        firebaseAuth.signOut();

        Auth.GoogleSignInApi.signOut(googleApiClient).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                if(status.isSuccess()){
                    goLogInScreen();
                }else{
                    Toast.makeText(getApplicationContext(),"No logout", Toast.LENGTH_LONG).show();
                }
            }
        });
    }








    /*CARGAMOS LOS DATOS DE FIREBASE*/


    public void setUserData(FirebaseUser userData) {
        txtNameUser.setText(userData.getDisplayName());
        txtEmailUser.setText(userData.getEmail());
        txtAuthUser.setText("ID: "+userData.getUid());

        Glide.with(this).load(userData.getPhotoUrl()).into(imageUserFirebase);
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }






    //Sobreescrito por La interface de IFragments
    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
