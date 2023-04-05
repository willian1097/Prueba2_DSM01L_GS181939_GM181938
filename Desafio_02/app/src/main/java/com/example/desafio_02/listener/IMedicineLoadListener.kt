package com.example.desafio_02.listener

import com.example.desafio_02.model.MedicineModel

interface IMedicineLoadListener {
    fun onMedicineLoadSuccess(medicineModelList: List<MedicineModel>?)
    fun onMedicineLoadFailed(message:String?)
}