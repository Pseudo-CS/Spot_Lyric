package com.spotlyric.app.data.remote.spotify

import android.net.Uri
import net.openid.appauth.AuthorizationServiceConfiguration

object SpotifyAuthConfig {

    val REDIRECT_URI: Uri = Uri.parse("spotlyric://callback")

    const val SCOPE: String = "user-read-currently-playing user-read-playback-state"

    val SERVICE_CONFIG: AuthorizationServiceConfiguration = AuthorizationServiceConfiguration(
        Uri.parse("https://accounts.spotify.com/authorize"),
        Uri.parse("https://accounts.spotify.com/api/token")
    )
}
