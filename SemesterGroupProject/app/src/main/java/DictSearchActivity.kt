import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView

class DictSearchActivity : AppCompatActivity() {

//    private val prevIntent = getIntent()
//    private lateinit var uid : String

    private lateinit var greetingTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //val theIntent = getIntent() as Intent
        //val user = theIntent.getStringExtra("theUser").toString()

        //greetingTextView = findViewById(R.id.languages_greeting)
        //greetingTextView.setText("Welcome, " + user)
        //ratingBar.isEnabled = false // this is how we disable it for learners
        //ratingBar.rating = 3.5f // this is how we set a rating I believe
    }

    companion object {
        const val TAG = "AudioDictionary"
    }
}