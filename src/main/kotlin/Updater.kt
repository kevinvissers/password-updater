import java.io.File

class Updater(private val configFileLocation: String, private val createBackup: Boolean = true) {
    companion object {
        private const val BACKUP_EXTENSION = "bak"
    }

    /**
     * Update password in all files provided in the config file
     * @param oldPassword Old password
     * @param newPassword New password
     */
    fun updateFiles(oldPassword: CharArray, newPassword: CharArray): Feedback {
        val status: MutableList<UpdateStatus> = mutableListOf()

        // validate passwords
        val passwordValid = validatePassword(oldPassword, newPassword)
        if(!passwordValid.success) {
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

                    // update the original file when the content changed
                    if(originalContent == updatedContent) {
                        status.updateStatus(it, false, "Old password not found in file")
                        return@loop
                    }

                    this.writeText(updatedContent)

                    status.updateStatus(it, true, "")
                }

            } catch (ex: Exception) {
                status.updateStatus(it, false, "Error occurred: ${ex.message}")
            }
        }

        return Feedback(!status.any { !it.success }, "", status)
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

        if(oldPasswordAsString.isBlank() || newPasswordAsString.isBlank()) {
            return Feedback(false, "Password can't be blank", listOf())
        }

        if(oldPasswordAsString == newPasswordAsString) {
            return Feedback(false, "New password = old password", listOf())
        }

        return Feedback(true, "", listOf())
    }

}
