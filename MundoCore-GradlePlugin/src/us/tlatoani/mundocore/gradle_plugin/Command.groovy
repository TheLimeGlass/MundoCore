package us.tlatoani.mundocore.gradle_plugin

class Command {
    String name
    String description
    String permissionSuffix =  'command'
    String permissionMessage = "You don't have the right permissions for that command!"

    void name(String name) {
        this.name = name
    }

    void description(String desc) {
        description = desc
    }

    void permissionSuffix(String suffix) {
        permissionSuffix = suffix
    }

    void permissionMessage(String message) {
        permissionMessage = message
    }
}
