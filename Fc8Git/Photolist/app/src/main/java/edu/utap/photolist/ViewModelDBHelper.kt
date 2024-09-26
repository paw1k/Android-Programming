package edu.utap.photolist

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import edu.utap.photolist.model.PhotoMeta

class ViewModelDBHelper {
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val rootCollection = "allPhotos"

    // If we want to listen for real time updates use this
    // .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
    private fun limitAndGet(query: Query,
                            resultListener: (List<PhotoMeta>)->Unit) {
        query
            .limit(100)
            .get()
            .addOnSuccessListener { result ->
                Log.d(javaClass.simpleName, "allNotes fetch ${result!!.documents.size}")
                // NB: This is done on a background thread
                resultListener(result.documents.mapNotNull {
                    it.toObject(PhotoMeta::class.java)
                })
            }
            .addOnFailureListener {
                Log.d(javaClass.simpleName, "allNotes fetch FAILED ", it)
                resultListener(listOf())
            }
    }
    /////////////////////////////////////////////////////////////
    // Interact with Firestore db
    // https://firebase.google.com/docs/firestore/query-data/order-limit-data
    fun fetchPhotoMeta(
        sortInfo: SortInfo,
        resultListener: (List<PhotoMeta>) -> Unit
    ) {
        // XXX Write me and use limitAndGet
        val fieldName: String = when(sortInfo.sortColumn){
            SortColumn.TITLE -> "pictureTitle"
            SortColumn.SIZE -> "byteSize"
        }
        val direction: Query.Direction = if (sortInfo.ascending) {
            Query.Direction.ASCENDING
        } else {
            Query.Direction.DESCENDING
        }
        val query = db.collection(rootCollection).orderBy(fieldName, direction)
        limitAndGet(query, resultListener)
    }

    // https://firebase.google.com/docs/firestore/manage-data/add-data#add_a_document
    fun createPhotoMeta(
        sortInfo: SortInfo,
        photoMeta: PhotoMeta,
        resultListener: (List<PhotoMeta>)->Unit
    ) {
        // XXX Write me: add photoMeta
        photoMeta.firestoreID = db.collection(rootCollection).document().id
        db.collection(rootCollection)
            .document(photoMeta.firestoreID)
            .set(photoMeta)
            .addOnSuccessListener {
                fetchPhotoMeta(sortInfo, resultListener)
            }
            .addOnFailureListener {
                resultListener(listOf())
            }
    }

    // https://firebase.google.com/docs/firestore/manage-data/delete-data#delete_documents
    fun removePhotoMeta(
        sortInfo: SortInfo,
        photoMeta: PhotoMeta,
        resultListener: (List<PhotoMeta>)->Unit
    ) {
        // XXX Write me.  Make sure you delete the correct entry.  What uniquely identifies a photoMeta?
        db.collection(rootCollection)
            .document(photoMeta.firestoreID)
            .delete()
            .addOnSuccessListener {
                fetchPhotoMeta(sortInfo, resultListener)
            }
            .addOnFailureListener {
                resultListener(listOf())
            }
    }
}