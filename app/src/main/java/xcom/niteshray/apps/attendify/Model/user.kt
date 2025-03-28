package xcom.niteshray.apps.attendify.Model

import android.os.Parcel
import android.os.Parcelable

data class user(
    val userId: String = "",
    val role: String = "",
    val name: String = "",
    val email: String = "",
    val collegeId: String = "",
    val department: String = "",
    val faceEmbedding: List<Float> = emptyList(),
    val studentClass: String = "", // Only for students
    val subjects: List<String> = emptyList() // Only for faculty
) : Parcelable {
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(role)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(collegeId)
        parcel.writeString(department)
        parcel.writeInt(faceEmbedding.size)
        faceEmbedding.forEach { parcel.writeFloat(it) }
        parcel.writeString(studentClass)
        parcel.writeStringList(subjects)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<user> {
        override fun createFromParcel(parcel: Parcel): user {
            val userId = parcel.readString() ?: ""
            val role = parcel.readString() ?: ""
            val name = parcel.readString() ?: ""
            val email = parcel.readString() ?: ""
            val collegeId = parcel.readString() ?: ""
            val department = parcel.readString() ?: ""
            val faceEmbeddingSize = parcel.readInt()
            val faceEmbedding = mutableListOf<Float>()
            repeat(faceEmbeddingSize) { faceEmbedding.add(parcel.readFloat()) }
            val studentClass = parcel.readString() ?: ""
            val subjects = parcel.createStringArrayList() ?: emptyList()

            return user(userId, role, name, email, collegeId, department, faceEmbedding, studentClass, subjects)
        }

        override fun newArray(size: Int): Array<user?> = arrayOfNulls(size)
    }
}




