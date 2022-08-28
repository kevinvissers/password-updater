import java.io.File
import java.util.*

class Updater(
    private val configFileLocation: String,
    private val createBackup: Boolean = true,
    private val dryRun: Boolean = false
) {
    companion object {
        private const val BACKUP_EXTENSION = "bak"
    }

    /**
     * Update password in all files provided in the config file
     * @param oldPassword Old password
     * @param newPassword New password
     */
    fun updateFiles(username: String, oldPassword: CharArray, newPassword: CharArray): Feedback {
        val status: MutableList<UpdateStatus> = mutableListOf()

        // validate passwords
        val passwordValid = validatePassword(oldPassword, newPassword)
        if (!passwordValid.success) {
            return passwordValid
        }

        // read which files to update
        val filesToUpdate = with(File(configFileLocation)) {
            if (!this.exists()) {
                return Feedback(false, "Config file $configFileLocation does not exist", listOf())
            }

            this.readLines()
        }

        // update password in each file
        filesToUpdate.forEach loop@{
            try {
                with(File(it)) {
                    // check if file exists
                    if (!this.exists()) {
                        status.updateStatus(it, false, "File does not exist")
                        return@loop
                    }

                    // create backup of file, just in case
                    if (createBackup) {
                        this.copyTo(File("$it.$BACKUP_EXTENSION"), overwrite = true)
                    }

                    // read content and replace the passwords
                    val originalContent = this.readText()
                    val updatedContent = originalContent.replace(String(oldPassword), String(newPassword))

                    // Base64 update
                    val base64OldPassword = base64Encode(username, oldPassword)
                    // println("old base64 - $base64OldPassword")
                    val base64NewPassword = base64Encode(username, newPassword)
                    // println("new base64 - $base64NewPassword")

                    val base64UpdatedContent = updatedContent.replace(base64OldPassword, base64NewPassword)

                    // Check if any changes happened
                    if ((originalContent == updatedContent) && (originalContent == base64UpdatedContent)) {
                        status.updateStatus(it, false, "Old password not found in file")
                        return@loop
                    }

                    var reason = ""
                    // update the original file when the content changed
                    if(!dryRun) {
                        this.writeText(base64UpdatedContent)
                        reason = "DRYRUN"
                    }

                    status.updateStatus(it, true, reason)
                }

            } catch (ex: Exception) {
                status.updateStatus(it, false, "Error occurred: ${ex.message}")
            }
        }

        return Feedback(!status.any { !it.success }, "", status)
    }

    /**
     * Encode username and password to Base64 string
     */
    private fun base64Encode(username: String, password: CharArray): String {
        val usernamePassword = "$username:${String(password)}"
        return Base64.getEncoder().encodeToString(usernamePassword.toByteArray())
    }


    /**
     * Append the result of the current file to the list
     */
    private fun MutableList<UpdateStatus>.updateStatus(fileLocation: String, success: Boolean, reason: String) {
        this.add(UpdateStatus(fileLocation, success, reason))
    }

    /**
     * Check if the password is valid
     */
    private fun validatePassword(oldPassword: CharArray, newPassword: CharArray): Feedback {
        val oldPasswordAsString = String(oldPassword)
        val newPasswordAsString = String(newPassword)

        if (oldPasswordAsString.isBlank() || newPasswordAsString.isBlank()) {
            return Feedback(false, "Password can't be blank", listOf())
        }

        if (oldPasswordAsString == newPasswordAsString) {
            return Feedback(false, "New password = old password", listOf())
        }

        return Feedback(true, "", listOf())
    }

}
