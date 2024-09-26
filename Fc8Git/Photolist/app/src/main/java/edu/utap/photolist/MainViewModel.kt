package edu.utap.photolist

import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import edu.utap.photolist.glide.Glide
import edu.utap.photolist.model.PhotoMeta
import edu.utap.photolist.view.TakePictureWrapper

enum class SortColumn {
    TITLE,
    SIZE
}
data class SortInfo(val sortColumn: SortColumn, val ascending: Boolean)
class MainViewModel : ViewModel() {
    // It is a real bummer that we need to put this here, but we do because
    // it is computed elsewhere, then we launch the camera activity
    // At that point our fragment can be destroyed, which means this has to be
    // remembered and restored.  Instead, we put it in the viewModel where we
    // know it will persist (and we can persist it)
    private var pictureUUID = ""
    // Only call this from TakePictureWrapper
    fun takePictureUUID(uuid: String) {
        pictureUUID = uuid
    }
    var pictureNameByUser = "" // String provided by the user
    // LiveData for entire note list, all images
    private var photoMetaList = MutableLiveData<List<PhotoMeta>>()
    private var sortInfo = MutableLiveData(
        SortInfo(SortColumn.TITLE, true))
    // Track current authenticated user
    private var currentAuthUser = invalidUser
    // Firestore state
    private val storage = Storage()
    // Database access
    private val dbHelp = ViewModelDBHelper()


    /////////////////////////////////////////////////////////////
    // Notes, memory cache and database interaction
    fun fetchPhotoMeta(resultListener:()->Unit) {
        dbHelp.fetchPhotoMeta(sortInfo.value!!) {
            photoMetaList.postValue(it)
            resultListener.invoke()
        }
    }
    fun observePhotoMeta(): LiveData<List<PhotoMeta>> {
        return photoMetaList
    }
    fun observeSortInfo(): LiveData<SortInfo> {
        return sortInfo
    }

    fun sortInfoClick(sortColumn: SortColumn,
                      resultListener: () -> Unit) {
        // XXX User has changed sort info
        var asc = !observeSortInfo().value?.ascending!!
        if (this.sortInfo.value?.sortColumn != sortColumn) asc = true
        sortInfo.postValue(SortInfo(sortColumn, asc))
        fetchPhotoMeta(resultListener)
    }

    // MainActivity gets updates on this via live data and informs view model
    fun setCurrentAuthUser(user: User) {
        currentAuthUser = user
    }
    fun removePhotoAt(position: Int) {
        // XXX Deletion requires two different operations.  What are they?
        val photoMeta = getPhotoMeta(position)
        storage.deleteImage(photoMeta.uuid)
        dbHelp.removePhotoMeta(sortInfo.value!!,photoMeta){
            fetchPhotoMeta {
                photoMetaList.postValue(it)
            }
        }
    }

    // Get a note from the memory cache
    fun getPhotoMeta(position: Int) : PhotoMeta {
        val note = photoMetaList.value?.get(position)
        return note!!
    }

    private fun createPhotoMeta(pictureTitle: String, uuid : String,
                                byteSize : Long) {
        val currentUser = currentAuthUser
        val photoMeta = PhotoMeta(
            ownerName = currentUser.name,
            ownerUid = currentUser.uid,
            uuid = uuid,
            byteSize = byteSize,
            pictureTitle = pictureTitle,
        )
        dbHelp.createPhotoMeta(sortInfo.value!!, photoMeta) {
            photoMetaList.postValue(it)
        }
    }

    /////////////////////////////////////////////////////////////
    // We can't just schedule the file upload and return.
    // The problem is that our previous picture uploads can still be pending.
    // So a note can have a pictureFileName that does not refer to an existing file.
    // That violates referential integrity, which we really like in our db (and programming
    // model).
    // So we do not add the pictureFileName to the note until the picture finishes uploading.
    // That means a user won't see their picture updates immediately, they have to
    // wait for some interaction with the server.
    // You could imagine dealing with this somehow using local files while waiting for
    // a server interaction, but that seems error prone.
    // Freezing the app during an upload also seems bad.
    fun pictureSuccess() {
        val photoFile = TakePictureWrapper.fileNameToFile(pictureUUID)
        // XXX Write me while preserving referential integrity
        storage.uploadImage(photoFile, pictureUUID) { byteSize ->
            if (byteSize > 0) {
                val pictureTitle = pictureNameByUser
                createPhotoMeta(pictureTitle, pictureUUID, byteSize)
            }
        }
    }
    fun pictureFailure() {
        // Note, the camera intent will only create the file if the user hits accept
        // so I've never seen this called
        pictureUUID = ""
        pictureNameByUser = ""
    }

    fun glideFetch(uuid: String, imageView: ImageView) {
        Glide.fetch(storage.uuid2StorageReference(uuid),
            imageView)
    }
}
