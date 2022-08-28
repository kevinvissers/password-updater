import javax.swing.*


object Main {
    private const val OFFSET = 20
    private const val Y_OFFSET = 10
    private const val COMPONENT_WIDTH = 150
    private const val COMPONENT_HEIGHT = 30

    private const val CONFIG_FILE_LOCATION = "config.txt"
    private const val CREATE_BACKUP = true

    private const val USERNAME = "JohnDoe"

    @JvmStatic
    fun main(args: Array<String>) {

        val frame = JFrame("Update Passwords")

        val oldPasswordField = JPasswordField()
        val newPasswordField = JPasswordField()

        val oldPasswordLabel = JLabel("Old password:")
        val newPasswordLabel = JLabel("New password:")

        val updateButton = JButton("Update")
        updateButton.addActionListener {
            val updater = Updater(CONFIG_FILE_LOCATION, createBackup = CREATE_BACKUP)
            val feedback = updater.updateFiles(USERNAME, oldPasswordField.password, newPasswordField.password)

            buildDialog(frame, feedback)
        }

        frame.setLayout(oldPasswordLabel, oldPasswordField, newPasswordLabel, newPasswordField, updateButton)
    }

    private fun buildDialog(frame: JFrame, feedback: Feedback) {
        val statusMessage = if(feedback.success) "Files updated successfully" else "Error during password update"
        var itemsStatusMessage = ""

        feedback.status.forEach {
            itemsStatusMessage += it.toString() + System.lineSeparator()
        }

        val icon = if(feedback.success) {
            JOptionPane.PLAIN_MESSAGE
        } else {
            JOptionPane.ERROR_MESSAGE
        }

        JOptionPane.showMessageDialog(frame,
            "$statusMessage - ${feedback.message}:${System.lineSeparator()}$itemsStatusMessage",
            "Update status",
            icon)
    }

    private fun JFrame.setLayout(vararg components: JComponent) {
        var iteration = 0

        // Calculate and set boundaries for each component
        components.forEachIndexed { index, jComponent ->
            val even = (index) % 2
            val x = if (index == components.lastIndex) {
                OFFSET + COMPONENT_WIDTH
            } else {
                OFFSET + (COMPONENT_WIDTH * even)
            }

            val y = OFFSET + ((COMPONENT_HEIGHT + Y_OFFSET) * iteration)
            if (even == 1) iteration++

            jComponent.setBounds(x, y, COMPONENT_WIDTH, COMPONENT_HEIGHT)
            this.add(jComponent)
        }

        // Calculate frame size
        val frameWidth = COMPONENT_WIDTH * 2 + (OFFSET * 3)
        val rows = (components.size / 2 + 1)
        val frameHeight = (COMPONENT_HEIGHT * rows) + ((OFFSET + Y_OFFSET) * rows)

        // Set frame configuration
        this.setSize(frameWidth, frameHeight)
        this.layout = null
        this.isVisible = true
        this.defaultCloseOperation = JFrame.EXIT_ON_CLOSE
    }
}
