package xcom.niteshray.apps.attendify.Model

data class Classess(
    val name: String = "",
    val facultyIds: List<String> = emptyList(),
    val studentIds: List<String> = emptyList(),
    val subjects: List<String> = emptyList()
)