package xcom.niteshray.apps.attendify.Model

import android.os.Parcel
import android.os.Parcelable

data class AttendanceSession(
    val sessionId: String,
    val deptId: String = "",
    val classId: String = "",
    val subject: String = "",
    val facultyId: String = "",
    val date: String = "",
    val qrCode: String = "",
    val sessionEnded: Boolean = false,
    val studentList: List<String> = emptyList()
) : Parcelable {

    constructor() : this("", "", "", "", "", "", "", false, emptyList())

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte(),
        parcel.createStringArrayList() ?: emptyList()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(sessionId)
        parcel.writeString(deptId)
        parcel.writeString(classId)
        parcel.writeString(subject)
        parcel.writeString(facultyId)
        parcel.writeString(date)
        parcel.writeString(qrCode)
        parcel.writeByte(if (sessionEnded) 1 else 0)
        parcel.writeStringList(studentList)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AttendanceSession> {
        override fun createFromParcel(parcel: Parcel): AttendanceSession {
            return AttendanceSession(parcel)
        }

        override fun newArray(size: Int): Array<AttendanceSession?> {
            return arrayOfNulls(size)
        }
    }
}

data class AttendanceRecord(
    val studentid: String = "",
    val status: String = "absent"
) : Parcelable {

    constructor() : this("", "")

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(studentid)
        parcel.writeString(status)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<AttendanceRecord> {
        override fun createFromParcel(parcel: Parcel): AttendanceRecord {
            return AttendanceRecord(parcel)
        }

        override fun newArray(size: Int): Array<AttendanceRecord?> {
            return arrayOfNulls(size)
        }
    }
}