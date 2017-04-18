package css.cis3334.firebaseauthentication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private TextView textViewStatus;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private Button buttonLogin;
    private Button buttonGoogleLogin;
    private Button buttonCreateLogin;
    private Button buttonSignOut;

    private FirebaseAuth mAuth; //Firebase Authentication Object
    private FirebaseAuth.AuthStateListener mAuthListener; //Listener Object for Firebase - listens for authentication state
    private GoogleApiClient mGoogleApiClient; //Google API Client
    private static final int GOOGLE_SIGN_IN_FLAG = 9001; //Used in Google sign in

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewStatus = (TextView) findViewById(R.id.textViewStatus);
        editTextEmail = (EditText) findViewById(R.id.editTextEmail);
        editTextPassword = (EditText) findViewById(R.id.editTextPassword);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);
        buttonGoogleLogin = (Button) findViewById(R.id.buttonGoogleLogin);
        buttonCreateLogin = (Button) findViewById(R.id.buttonCreateLogin);
        buttonSignOut = (Button) findViewById(R.id.buttonSignOut);

        //Firebase AuthStateListener - to track who signs in and out
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() { //Declaration for mAuth and mAuthListener Objects
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser(); //Gets current user state and changes sign-in state appropriately
                if (user != null) {
                    // User is signed in
                    Log.d("CIS3334", "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d("CIS3334", "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };


        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build(); //Builds the Google sign-in API


        buttonLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { //A Listener for the Login Button, allows a user to "Login" into Firebase
                //Log.d("CIS3334", "normal login "); //Logs in a user normally with Firebase
                signIn(editTextEmail.getText().toString(), editTextPassword.getText().toString());
            }
        });

        buttonCreateLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { //A listner for the Create User Button, creates a new user account in Firebase
                //Log.d("CIS3334", "Create Account "); //Creates a new user account in Firebase
                createAccount(editTextEmail.getText().toString(), editTextPassword.getText().toString());
            }
        });

        buttonGoogleLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { //Allows a user to Login using Google
                //Log.d("CIS3334", "Google login "); //Starts the sign in process using new Google activity
                googleSignIn();
            }
        });

        buttonSignOut.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { //A listener that signs a user out of the Firebase authentication
                //Log.d("CIS3334", "Logging out - signOut "); //Signs user out of database (but keeps login credentials)
                signOut();
            }
        });


    }

    /*
    * onStart() - When the activity starts, start the FirebaseAuth listener to listen for logins
    **/
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    /*
    * onStop() - When an the activity stops, remove the FirebaseAuth listener from the activity
    **/
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    /*
    * onConnectionFailed() - If Google login fails, generate a message notifying the user of
    * the failed login.
    **/
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) { //Used in Google login
        //Log.w("CIS3334", "onConnectionFailed:failed"); //Notifies log that the connection to Google account failed
        //Toast.makeText(MainActivity.this, "FAIL", Toast.LENGTH_LONG).show();
        textViewStatus.setText("Status: Google Account Connection Fail");
    }

    /*
    * firebaseAuthWithGoogle() - takes the results of the Google sign-in and transfers the results\
    * to firebase. If a user succeeds with the login, the user login credentials will be saved to
    * firebase.
    **/
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        //Log.d("CIS334", "firebaseAuthWithGoogle:" + acct.getId()); //Completes Firebase login using Google account

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null); //The Google sign-in credentials
        mAuth.signInWithCredential(credential) //transferred to the Firebase Authenticator
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Log.d("CIS334", "signInWithCredential:onComplete:" + task.isSuccessful()); //Notifies that login was successful
                        textViewStatus.setText("Status: Sign-in Successful");


                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            //Log.w("CIS334", "signInWithCredential", task.getException()); //Notifies that login failed for some reason
                            //Toast.makeText(MainActivity.this, "Authentication failed.",
                                    //Toast.LENGTH_SHORT).show();
                            textViewStatus.setText("Status: Authentication Failed");
                        }
                        // ...
                    }
                });
    }

    /*
    * createAccount() - takes in an email address and password, validates them, and creates a new user
    * in the Firebase database.
    **/
    private void createAccount(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Log.d("CIS3334", "createUserWithEmail:onComplete:" + task.isSuccessful()); //Notifies that creation of user was successful
                        textViewStatus.setText("Status: Account Creation Successful");

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            //Toast.makeText(MainActivity.this, "Authentication failed.",
                                    //Toast.LENGTH_SHORT).show();
                            textViewStatus.setText("Status: Authentication Failed");
                        }

                        // ...
                    }
                });
    }

    /*
    * signIn() - takes in an email address and password, validates them, and then signs a
    * user into the Firebase database.
    **/
    private void signIn(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        //Log.d("CIS3334", "signInWithEmail:onComplete:" + task.isSuccessful()); //Notifies that sign-in was complete
                        textViewStatus.setText("Status: Sign-in Successful");

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            //Log.w("CIS3334", "signInWithEmail", task.getException()); //Notifies that sign-in attempt failed for some reason
                            //Toast.makeText(MainActivity.this, "Authentication failed.",
                                    //Toast.LENGTH_SHORT).show();
                            textViewStatus.setText("Status: Authentication Failed");
                        }

                        // ...
                    }
                });
    }

    /*
    * signOut() - signs a user out of the Firebase database. Login info is saved though.
    **/
    private void signOut () {
        mAuth.signOut();
    }

    /*
    * googleSignIn() - starts a new activity to sign a user in using Google. Returns the result
    * of the login process (successful or failed).
    **/
    private void googleSignIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN_FLAG);
    }

    /*
    * onActivityResult() - When the Google sign-in is finished, complete the sign-in process
    * by connecting Google to Firebase.
    **/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == GOOGLE_SIGN_IN_FLAG) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed, update UI appropriately
                // ...
            }
        }
    }
}
