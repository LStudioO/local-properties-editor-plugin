package com.github.lstudioo.propertieseditor.repository

/**
 * Observer interface for the property repository.
 * 
 * This interface allows components to be notified when properties are reloaded
 * in the repository. Implementing classes can register with a PropertyRepository
 * to receive notifications when property data changes, enabling reactive UI updates
 * and other responses to configuration changes.
 */
interface PropertyRepositoryObserver {
    /**
     * Called when properties have been reloaded in the repository.
     * 
     * This method is invoked after the repository has completed loading or reloading
     * properties from the underlying storage. Observers should use this notification
     * to refresh their state or update UI components with the latest property values.
     */
    fun onPropertiesReloaded()
}
