*** Settings ***
Library    AppiumLibrary

Test Setup    Setup
Test Teardown    Close Application

*** Variables ***
${song_name}    test name
${AUDIO_URL}    https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3
${IMAGE_URL}    https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Ffreepngimg.com%2Fthumb%2Fleaf%2F30436-8-leaf-transparent.png&f=1&nofb=1&ipt=58227c5b3bb209272fddc9964df6398aa187ecdf79f144e5766b10dde7b35958&ipo=image
${second_song_name}    test name 2
${second_AUDIO_URL}    https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3
${second_IMAGE_URL}    https://external-content.duckduckgo.com/iu/?u=https%3A%2F%2Fwww.pngall.com%2Fwp-content%2Fuploads%2F5%2FSingle-Plant-Leaf-PNG-File.png&f=1&nofb=1&ipt=1240cf145252d0fa6f56cd5246ad0ad1cfe7ec7e32d845ede5ecd8cd83a2b5c9&ipo=images


*** Keywords ***
Setup
    Open Application    http://localhost:4723/wd/hub    
    ...    automationName=Appium
    ...    platformName=Android
    ...    noReset=${True}
    ...    app=/Users/adam/Desktop/studia/soundsAppTests/app.apk

Wait and click
    [Arguments]    ${locator}
    Wait Until Element Is Visible    ${locator}
    Click Element    ${locator}    

Wait and input text
    [Arguments]    ${locator}    ${text}
    Wait Until Element Is Visible    ${locator}
    Input Text    ${locator}    ${text}

*** Test Cases ***
Clear app
    Close Application
    Open Application    http://localhost:4723/wd/hub    
    ...    automationName=Appium
    ...    platformName=Android
    ...    noReset=${False}
    ...    fullReset=${True}
    ...    app=/Users/adam/Desktop/studia/soundsAppTests/app.apk

Dodawanie nowego utworu
    Wait and click    //android.widget.TextView[@text="Add"]
    Wait and input text    //androidx.compose.ui.platform.ComposeView/android.view.View/android.view.View/android.widget.EditText[1]    ${song_name}
    Wait and input text    //androidx.compose.ui.platform.ComposeView/android.view.View/android.view.View/android.widget.EditText[2]    ${AUDIO_URL}
    Wait and input text    //androidx.compose.ui.platform.ComposeView/android.view.View/android.view.View/android.widget.EditText[3]    ${IMAGE_URL}
    Sleep    1
    Page should contain element    //android.widget.ImageView[@content-desc="Image"]
    Wait and click    //android.widget.TextView[@text="Save"]
    Sleep    1
    Page should contain element    //android.widget.TextView[@text="Play"]
    Page should contain element    //android.widget.TextView[@text="${song_name}"]
    Page should contain element    //android.view.View[@content-desc="Image"]

Testowanie dodanego utworu
    Wait and click    //android.widget.TextView[@text="${song_name}"]
    Page should contain element    //android.view.View[@content-desc="Selected"]
    Wait and click    //android.widget.TextView[@text="Play"]
    Sleep    3
    Page should contain element    //android.widget.TextView[@text="Stop"]
    Wait and click    //android.widget.TextView[@text="Stop"]
    Page should contain element    //android.widget.TextView[@text="Play"]
    Wait and click    //android.widget.TextView[@text="${song_name}"]
    Page should not contain element    //android.view.View[@content-desc="Selected"]
    Wait and click    //android.widget.TextView[@text="Play"]
    Sleep    3
    Page should contain element    //android.widget.TextView[@text="Stop"]
    Wait and click    //android.widget.TextView[@text="Stop"]

Dodawanie niepełnego utworu
    Wait and click    xpath=//android.widget.TextView[@text="Add"]
    Wait and input text    //androidx.compose.ui.platform.ComposeView/android.view.View/android.view.View/android.widget.EditText[1]    ${second_song_name}
    Wait and click    //android.widget.TextView[@text="Save"]
    Sleep    1
    Page should not contain element    //android.widget.TextView[@text="${second_song_name}"]

Porzucenie dodawania utworu
    Wait and click    xpath=//android.widget.TextView[@text="Add"]
    Wait and input text    //androidx.compose.ui.platform.ComposeView/android.view.View/android.view.View/android.widget.EditText[1]    ${second_song_name}
    Wait and click    //android.widget.TextView[@text="Back"]
    Sleep    1
    Page should not contain element    //android.widget.TextView[@text="${second_song_name}"]

Dodawanie drugiego utworu
    Wait and click    xpath=//android.widget.TextView[@text="Add"]
    Wait and input text    //androidx.compose.ui.platform.ComposeView/android.view.View/android.view.View/android.widget.EditText[1]    ${second_song_name}
    Wait and input text    //androidx.compose.ui.platform.ComposeView/android.view.View/android.view.View/android.widget.EditText[2]    ${second_AUDIO_URL}
    Wait and input text    //androidx.compose.ui.platform.ComposeView/android.view.View/android.view.View/android.widget.EditText[3]    ${second_IMAGE_URL}
    Sleep    1
    Page should contain element    //android.widget.ImageView[@content-desc="Image"]
    Wait and click    //android.widget.TextView[@text="Save"]
    Sleep    1
    Page should contain element    //android.widget.TextView[@text="Play"]
    Page should contain element    //android.widget.TextView[@text="${second_song_name}"]
    Page should contain element    //android.view.View[@content-desc="Image"]

Testowanie dodanego drugiego utworu
    Wait and click    //android.widget.TextView[@text="${song_name}"]
    Wait and click    //android.widget.TextView[@text="${second_song_name}"]
    @{songs}    Get Webelements    //android.view.View[@content-desc="Selected"]
    ${songs_length}    Get Length    ${songs}
    Should Be Equal As Numbers    ${songs_length}    2
    Wait and click    //android.widget.TextView[@text="Play"]
    Sleep    3
    Page should contain element    //android.widget.TextView[@text="Stop"]
    Wait and click    //android.widget.TextView[@text="Stop"]
    Page should contain element    //android.widget.TextView[@text="Play"]
    Wait and click    //android.widget.TextView[@text="${song_name}"]
    Wait and click    //android.widget.TextView[@text="${second_song_name}"]
    Page should not contain element    //android.view.View[@content-desc="Selected"]
    Wait and click    //android.widget.TextView[@text="Play"]
    Sleep    3
    Page should contain element    //android.widget.TextView[@text="Stop"]
    Wait and click    //android.widget.TextView[@text="Stop"]

Powrót z ustawień
    Wait and click    //android.widget.TextView[@text="Settings"]
    Wait and click    //android.widget.SeekBar[@text="10.0"]
    Wait and click    //android.widget.TextView[@text="Back"]
    Wait and click    //android.widget.TextView[@text="${song_name}"]
    Wait and click    //android.widget.TextView[@text="Play"]
    Sleep    7
    Page should contain element    //android.widget.TextView[@text="Stop"]

Ustawienie timera
    Wait and click    //android.widget.TextView[@text="Settings"]
    Wait and click    //android.widget.SeekBar[@text="10.0"]
    Wait and click    //android.widget.TextView[@text="Save"]
    Wait and click    //android.widget.TextView[@text="${song_name}"]
    Wait and click    //android.widget.TextView[@text="Play"]
    Sleep    7
    Page should not contain element    //android.widget.TextView[@text="Stop"]

Ustawienie głośności
    Wait and click    //android.widget.TextView[@text="Settings"]
    Wait and click    //android.widget.SeekBar[@text="1.0"]
    Wait and click    //android.widget.TextView[@text="Save"]
    Wait and click    //android.widget.TextView[@text="${song_name}"]
    Wait and click    //android.widget.TextView[@text="Play"]
    Sleep    2

Włączenie tutorialu
    Wait and click    //android.widget.TextView[@text="Settings"]
    Wait and click    //android.widget.TextView[@text="Play Tutorial"]
    Sleep    1
    Page should contain element    	//android.widget.VideoView
    Wait and click    //android.widget.TextView[@text="Back"]

Kliknięcie linku do githuba
    Wait and click    //android.widget.TextView[@text="Settings"]
    Wait and click    //android.widget.TextView[@text="Go to GitHub"]
    Sleep    5
    Page should contain element    //android.webkit.WebView[@text="GitHub - pawlowskia/SoundsApp"]
