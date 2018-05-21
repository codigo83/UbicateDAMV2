package com.example.carlo.ubicatedam;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener{

    private static final int GOOGLE_OK = 777;
    private GoogleApiClient googleApiClient;
    private SignInButton signInButton;
    private ProgressBar progressBar;

    /*PARA FIREBASE
     * En cada lugar donde queramos manejar la autenticación
     * o información del usuario debemos realizar lo siguiente
     *
     * La autenticacion de firebase y su listener correspondiente
     *
     * */
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener firebaseListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        /*Para obtener a demas el correo del usuario autenticado añadimos el método requestEmail()
         * Obtenemos el token para firebase con requestIdToken
         * */
        GoogleSignInOptions gso= new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();



        /*Builder: Primer parametro el contexto*/
        /*enableAutoManage: Primer parametro el contexto y el segundo
        es quien se encargara de escuchar si algo salio mal(En este caso la misma activity)*/

        /*GoogleApiClient es el intermediario entre las aplicaciones de google y nuestra aplicación*/
        /*addApi: Primer parametro necesita la autenticación, como segundo un objeto de opciones que define como autenticarnos (GoogleSingInOptions) */
        googleApiClient= new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();



        signInButton= findViewById(R.id.btnSingGoogle);
        /*PERSONALIZACION de boton
         *
         * signInButton.setSize(SignInButton.SIZE_WIDE)   Boton mas grande
         * signInButton.setSize(SignInButton.ICON_ONLY)   Solo con el icono de google
         *
         * signInButton.setColorScheme(SignInButton.COLOR_DARK)  pone un boton con fondo oscuro
         *
         * */
        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setColorScheme(SignInButton.COLOR_DARK);




        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Este intent abrira el inicio de sesion para una cuenta google
                Intent intent= Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(intent, GOOGLE_OK);
            }
        });


        /*FIREBASE
         *onAuthStateChanged se ejecuta cuando cambia el estado de la autenticacion
         *
         * en el metodo onStart el oyente escucha los cambios de estado
         * Cuando empieze a escuchar en algun momento tendremos que deternlo, eso sera en el onStop
         * */
        firebaseAuth= FirebaseAuth.getInstance();
        firebaseListener= new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //Comprobar si estamos autenticados
                FirebaseUser user= firebaseAuth.getCurrentUser();
                if(user != null){
                    //Ahora si enviamos al mainActivity
                    goMainScreen();
                }
            }
        };

        progressBar= findViewById(R.id.idProgresBar);

    }



    ///////////////////////listener firebase///////////////////////////////////////////////////
    @Override
    protected void onStart() {
        super.onStart();
        //Adañimos el listener al atuh de firebase
        firebaseAuth.addAuthStateListener(firebaseListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //eliminamos al oyente
        if(firebaseListener != null){
            firebaseAuth.removeAuthStateListener(firebaseListener);
        }
    }
    ////////////////////////listener firebase***///////////////////////////////////////////////////



    //////////////////////////////Conexion con google////////////////////////////////////////////////

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //Este método se ejecuta cuando algo sale mal en la conexión

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Comprobamos que el requestcode es el corecto
        if(requestCode == GOOGLE_OK){
            //Almacenamos el resultado
            GoogleSignInResult result= Auth.GoogleSignInApi.getSignInResultFromIntent(data);

            //Gestionamos el resultado
            handleSingResult(result);

        }

    }

    ///////////////////////////Conexion con google***////////////////////////////////////////////




    ///////////////////////////////Metodos propios////////////////////////////////////////////////////
    private void handleSingResult(GoogleSignInResult result) {
        //Comprobar si la operacion ha tenido exito
        if(result.isSuccess()){
            //todo esta bien abrimos la activity principal donde mostraremos los datos
            //Gestionamos la autenticacion con firebase, a este método solo le mandamos la cuenta
            firebaseAuthWithGoogle(result.getSignInAccount());

        }else{
            Log.e("ERRORFIREBASE", result.getStatus().toString());
            Toast.makeText(this, "No se pudo iniciar", Toast.LENGTH_SHORT).show();
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount signInAccount) {

        progressBar.setVisibility(View.VISIBLE);//Aparece progresbar
        signInButton.setVisibility(View.GONE);//Desaparece boton google

        /*
         * Nos creamos una credencial a la que le pasamos el token que obtenemos del objeto cuenta
         * El segundo parametro es el accessToken que no es necesario, por eso le pasamos null
         * */
        AuthCredential credential= GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);

        /*Una vez tenemos la credencial podemos autenticarnos con firebase
         * y añadirle otro listener que nos avisara cuando termine*/
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                progressBar.setVisibility(View.GONE);//Desaparece
                signInButton.setVisibility(View.VISIBLE);//

                //No se pregunta cuando ha sido exitoso , eso s hace en otro listener
                if(!task.isSuccessful()){
                    Toast.makeText(LoginActivity.this, "No autenticacion con FIREBASE", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    /*Redirige a Main cuando se logea el usuario*/
    private void goMainScreen() {
        Intent intent= new Intent(this, MainActivity.class);
        //No se que hace esto...(tiene los flags necesarios para que nunca se quede una como anterior de la otra ¿¿¿¿¿¿WTF??????)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    /////////////////////////////////Metodos propios***////////////////////////////////////////////////



}
