package com.plcoding.typesafecomposenavigation

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.SoundPool
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.MediaStore.Audio
import android.provider.MediaStore.Video
import android.util.Log
import android.widget.MediaController
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.estimateAnimationDurationMillis
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import com.plcoding.typesafecomposenavigation.ui.theme.TypeSafeComposeNavigationTheme
import kotlinx.serialization.Serializable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Upsert
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.runBlocking
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.round
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

var globalVolume = 1.0f
var globalStop = 0
var mediaPlayers = emptyList<MediaPlayer>()

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TypeSafeComposeNavigationTheme {
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = MainScreen,
                    enterTransition = {
                        expandHorizontally(
                            animationSpec = tween(1000)
                        )
                    }
                ) {
                    composable<MainScreen> {
                        MainScreen(navController = navController)
                    }
                    composable<AddScreen> {
                        AddScreen(navController = navController)
                    }
                    composable<SettingsScreen> {
                        SettingsScreen(navController = navController)
                    }
                    composable<TutorialScreen> {
                        TutorialScreen(navController = navController)
                    }
                }
            }
        }
    }
}

@Serializable
object MainScreen

@Serializable
object AddScreen

@Serializable
object SettingsScreen

@Serializable
object TutorialScreen

@Entity
data class AudioFile(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val imageUrl: String,
    val audioUrl: String,
    val isSelected: Boolean = false
)

@Dao
interface AudioFileDao {
    @Upsert
    suspend fun upsert(audioFile: AudioFile)
    @Delete
    suspend fun delete(audioFile: AudioFile)
    @Query("SELECT * FROM AudioFile")
    fun getAll(): List<AudioFile>
}

@Database(
    entities = [AudioFile::class],
    version = 1
)
abstract class AudioFileDatabase : RoomDatabase() {
    abstract fun audioFileDao(): AudioFileDao
}

class MainViewModel : ViewModel() {
    fun startTimer(){
        if(globalStop != 0){
            viewModelScope.launch(Dispatchers.Main) {
                delay(globalStop * 1000L)
                for (mediaPlayer in mediaPlayers){
                    mediaPlayer.stop()
                    mediaPlayer.release()
                }
                mediaPlayers = emptyList()
                Log.d("MainView", "timer stopped")
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MainScreen(navController: NavController) {
    val audioFiles = remember { mutableStateListOf<AudioFile>() }
    val viewModel : MainViewModel = viewModel()
    mediaPlayers = emptyList()
    var isPlaying by remember { mutableStateOf(false) }
    val dao = Room.databaseBuilder(
        LocalContext.current,
        AudioFileDatabase::class.java,
        "audio_file_db"
    ).allowMainThreadQueries().build().audioFileDao()
    audioFiles.clear()
    audioFiles.addAll(dao.getAll())

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    for (mediaPlayer in mediaPlayers){
                        mediaPlayer.stop()
                        mediaPlayer.release()
                    }
                    mediaPlayers = emptyList()
                    navController.navigate(AddScreen)
                }) {
                    Text(text = "Add")
                }
                Button(onClick = {
                    for (mediaPlayer in mediaPlayers){
                        mediaPlayer.stop()
                        mediaPlayer.release()
                    }
                    mediaPlayers = emptyList()
                    navController.navigate(SettingsScreen)

                }) {
                    Text(text = "Settings")
                }

                Button(onClick = {
                    if (isPlaying){
                        isPlaying = false
                        for (mediaPlayer in mediaPlayers){
                            mediaPlayer.stop()
                            mediaPlayer.release()
                        }
                        mediaPlayers = emptyList()
                    }
                    else {
                        isPlaying = true
                        viewModel.startTimer()
                        Log.d("MainView", "timer started")

                        for (audioFile in audioFiles){
                            //    val url = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"
                            //    val mediaPlayer = MediaPlayer().apply {
                            //        setAudioAttributes(
                            //            AudioAttributes.Builder()
                            //                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            //                .setUsage(AudioAttributes.USAGE_MEDIA)
                            //                .build()
                            //        )
                            //        setDataSource(url)
                            //        prepare() // might take long! (for buffering, etc)
                            //        start()
                            //    }
                            if (!audioFile.isSelected)
                                continue
                            val url = audioFile.audioUrl
                            val mediaPlayer = MediaPlayer().apply {
                                setAudioAttributes(
                                    AudioAttributes.Builder()
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .build()
                                )
                                setDataSource(url)
                                setVolume(globalVolume, globalVolume)
                                isLooping = true

                                prepare()
                                start()
                            }
                            mediaPlayers = mediaPlayers.plus(mediaPlayer)
                        }

                    }
                }) {
                    Text(text = if (isPlaying) "Stop" else "Play")
                }
            }
        }
    ) {
        LazyColumn (
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 24.dp)
        ) {
            items(audioFiles.size) { i ->
                Row (
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            audioFiles[i] =
                                audioFiles[i].copy(isSelected = !audioFiles[i].isSelected)
                        }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ){
                    val audioFile = audioFiles[i]
                    AsyncImage(
                        model = audioFile.imageUrl,
                        contentDescription = "Image",
                        modifier = Modifier.size(50.dp)
                    )
                    Text(text = audioFile.name, modifier = Modifier.padding(start = 16.dp))
                    if (audioFile.isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = Color.Green,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    else {
                        Spacer(modifier = Modifier.size(24.dp))
                    }
            }
        }

        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AddScreen(navController: NavController) {
    val name = remember { mutableStateOf("") }
    val imageUrl = remember { mutableStateOf("") }
    val audioUrl = remember { mutableStateOf("") }
    val pickSongLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        Log.d("AddScreen", uri.toString())
        if (uri != null) {
            // get audio file path
            audioUrl.value = uri.toString()
        }
    }
    val dao = Room.databaseBuilder(
        LocalContext.current,
        AudioFileDatabase::class.java,
        "audio_file_db"
    ).allowMainThreadQueries().build().audioFileDao()

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    navController.navigate(MainScreen)
                }) {
                    Text(text = "Back")
                }
                Button(onClick = {
                    if (name.value == "")
                        name.value = "Audio File"

                    if(audioUrl.value != ""){
                        runBlocking {
                            dao.upsert(AudioFile(name = name.value, imageUrl = imageUrl.value, audioUrl = audioUrl.value))
                        }
                    }
                    navController.navigate(MainScreen)
                }) {
                    Text(text = "Save")
                }
            }
    }) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = imageUrl.value,
                contentDescription = "Image",
                modifier = Modifier
                    .padding(16.dp)
                    .size(200.dp)
            )
            TextField(
                value = name.value,
                onValueChange = { name.value = it},
                label = { Text("Name") },
                maxLines = 1,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
            TextField(
                value = audioUrl.value,
                onValueChange = { audioUrl.value = it},
                label = { Text("Audio URL") },
                maxLines = 1,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
            TextField(
                value = imageUrl.value,
                onValueChange = { imageUrl.value = it},
                label = { Text("Image URL") },
                maxLines = 1,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun SettingsScreen(navController: NavController) {
//    Time picker for when to stop playing and slider for volume
    val stop = remember { mutableIntStateOf(10) }
    val volume = remember { mutableFloatStateOf(1.0f) }
    val uriHandler = LocalUriHandler.current
    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    navController.navigate(MainScreen)
                }) {
                    Text(text = "Back")
                }
                Button(onClick = {
                    globalVolume = volume.floatValue
                    globalStop = stop.intValue
                    navController.navigate(MainScreen)
                }) {
                    Text(text = "Save")
                }
            }
        }
    ) {
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ){
            Text(text = "Stop playing after " + stop.intValue + "secs.", modifier = Modifier.padding(16.dp))
            Slider(
                value = stop.intValue.toFloat(),
                onValueChange = { stop.intValue = it.toInt() },
                valueRange = 0.0f..10.0f,
                steps = 99,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
            Text(text = "Volume: " + round(volume.floatValue * 100) + "%.", modifier = Modifier.padding(16.dp))
            Slider(
                value = volume.floatValue,
                onValueChange = { volume.floatValue = it },
                valueRange = 0.0f..1.0f,
                steps = 9,

                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
//            play a video from R.res.raw.tutorial.mp4
            Button(
                onClick = {
                    navController.navigate(TutorialScreen)
                },
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ){
                Text(text = "Play Tutorial")
            }
            Button(
                onClick = {
                    uriHandler.openUri("https://github.com/pawlowskia/SoundsApp.git")
                },
                modifier = Modifier.padding(16.dp).fillMaxWidth()
            ) {
                Text(text = "Go to GitHub")
            }
        }
    }
}

@Composable
fun VideoPlayer(
    videoUri: Uri
) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        factory = { context ->
            VideoView(context).apply {
                setVideoURI(videoUri)

                val mediaController = MediaController(context)
                mediaController.setAnchorView(this)

                setMediaController(mediaController)

                setOnPreparedListener {
                    start()
                }
            }
        })

}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun TutorialScreen(navController: NavController) {
    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp, horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(onClick = {
                    navController.navigate(MainScreen)
                }) {
                    Text(text = "Back")
                }
            }
        }
    ) {
        VideoPlayer(Uri.parse("android.resource://" + LocalContext.current.packageName + "/" + R.raw.tutorial))
    }
}