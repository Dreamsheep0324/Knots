package com.tang.prm.domain.usecase

import com.tang.prm.domain.model.CustomType
import com.tang.prm.domain.model.CustomCategories
import com.tang.prm.domain.repository.ContactRepository
import com.tang.prm.domain.repository.CustomTypeRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DeleteCustomTypeUseCase @Inject constructor(
    private val customTypeRepository: CustomTypeRepository,
    private val contactRepository: ContactRepository,
    private val cleanCustomTypeUseCase: CleanCustomTypeUseCase
) {
    suspend operator fun invoke(type: CustomType) {
        customTypeRepository.deleteTypeById(type.id)
        when (type.category) {
            CustomCategories.RELATIONSHIP -> {
                val contacts = contactRepository.getFilteredContacts(null, null, type.name).first()
                contacts.forEach { contact ->
                    contactRepository.updateContact(contact.copy(
                        relationship = null,
                        updatedAt = System.currentTimeMillis()
                    ))
                }
            }
            CustomCategories.EDUCATION -> {
                val contacts = contactRepository.getAllContacts().first()
                contacts.filter { it.education == type.name }.forEach { contact ->
                    contactRepository.updateContact(contact.copy(
                        education = null,
                        updatedAt = System.currentTimeMillis()
                    ))
                }
            }
            CustomCategories.HOBBY, CustomCategories.HABIT, CustomCategories.DIET, CustomCategories.SKILL ->
                cleanCustomTypeUseCase.removeFromListFieldAll(type.category, type.name)
        }
    }
}
