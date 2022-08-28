import org.testng.annotations.Test
import java.io.File

class UpdaterTest {

    companion object {
        private const val JOHN_DOE = "JohnDoe"
        private const val OLD_PASSWORD = "OldPassword"
        private const val NEW_PASSWORD = "NewPassword"
    }

    @Test
    fun tryUpdate() {
        val updater = Updater(configFileLocation = "test_config.txt", createBackup = true, dryRun = true)
        val result = updater.updateFiles(JOHN_DOE, OLD_PASSWORD.toCharArray(), NEW_PASSWORD.toCharArray())
        println(result)

        assert(!result.success)
        assert(result.message == "")

        println("test_files/one_password.txt")
        assert(result.status[0].success)
        assert(result.status[0].reason == "")
        assert(result.status[0].fileName == "test_files/one_password.txt")

        println("test_files/multiple_passwords.xml")
        assert(result.status[1].success)
        assert(result.status[1].reason == "")
        assert(result.status[1].fileName == "test_files/multiple_passwords.xml")

        println("test_files/no_password.txt")
        assert(!result.status[2].success)
        assert(result.status[2].reason == "Old password not found in file")
        assert(result.status[2].fileName == "test_files/no_password.txt")

        println("test_files/base64_password")
        assert(result.status[3].success)
        assert(result.status[3].reason == "")
        assert(result.status[3].fileName == "test_files/base64_password")

        println("test_files/base64_normal_password.txt")
        assert(result.status[4].success)
        assert(result.status[4].reason == "")
        assert(result.status[4].fileName == "test_files/base64_normal_password.txt")
    }

    @Test
    fun update() {
        val updater = Updater(configFileLocation = "test_config.txt", createBackup = true, dryRun = false)
        val result = updater.updateFiles(JOHN_DOE, OLD_PASSWORD.toCharArray(), NEW_PASSWORD.toCharArray())
        println(result)

        val files = listOf(
            "test_files/one_password.txt",
            "test_files/multiple_passwords.xml",
            "test_files/no_password.txt",
            "test_files/base64_password",
            "test_files/base64_normal_password.txt"
        )

        files.forEach {
            with(File(it)) updated@ {
                val updatedText = this@updated.readText()

                with(File("$it.updated")) {
                    val expectedText = this.readText()

                    println("$it - $it.updated")
                    println("updateText: $updatedText")
                    println(System.lineSeparator())
                    println("expected text: $expectedText")
                    assert(expectedText == updatedText)
                }
            }
        }
    }

    @Test
    fun nonExistingConfig() {
        val updater = Updater(configFileLocation = "test_config_non_existing.txt", createBackup = true, dryRun = true)
        val result = updater.updateFiles(JOHN_DOE, OLD_PASSWORD.toCharArray(), NEW_PASSWORD.toCharArray())
        println(result)
        assert(!result.success)
        assert(result.message == "Config file test_config_non_existing.txt does not exist")
        assert(result.status == listOf<String>())
    }
}
