import org.testng.annotations.Test

class UpdaterTest {

    @Test
    fun tryUpdate() {
        val updater = Updater(configFileLocation = "test_config.txt", createBackup = true)
        val result = updater.updateFiles("JohnDoe", "OldPassword".toCharArray(), "NewPassword".toCharArray())



    }
}
