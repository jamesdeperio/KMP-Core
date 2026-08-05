package shared.core.platform

expect object SystemAction {
    fun exitApp()

    fun openLink(link: String)
}
