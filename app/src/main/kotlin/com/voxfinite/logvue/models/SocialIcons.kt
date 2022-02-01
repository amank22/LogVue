package com.voxfinite.logvue.models

enum class SocialIcons(val icon: String, val url: String) {
    Twitter(
        "icons/social/social_twitter.svg",
        "https://twitter.com/Aman22Kapoor"
    ),
    Github(
        "icons/social/social_github.svg",
        "https://github.com/amank22/LogVue"
    ),
    GithubIssues(
        "icons/social/social_github.svg",
        "https://github.com/amank22/LogVue/issues/new"
    ),
    Linkedin("icons/social/social_linkedIn.svg", "https://www.linkedin.com/in/amank22/"),
    Email("icons/ico-email.svg", "mailto://kapoor.aman22@gmail.com"),
    ;

    companion object {
        val DefaultIcons = listOf(Twitter, Github, Linkedin, Email)
    }
    // object SocialFacebook : SocialIcons("icons/social/social_facebook.svg", "")
}
