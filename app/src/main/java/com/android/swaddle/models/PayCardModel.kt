package com.android.swaddle.models

import com.google.gson.annotations.SerializedName


class PayCardModel {

    var name: String? = ""
    var id: Int? = 0
    var card_id: String? = ""
    var last_four: String? = ""
    var is_default: Int? = 0
    var isSelected: Boolean = false

    companion object {

        fun copyFromNewModel(item: CardModelNewItem): PayCardModel {
            val payModel = PayCardModel()
            payModel.id = item.id?.toInt()
            payModel.is_default = item.isDefault?.toInt()
            payModel.last_four = item.lastFour
            payModel.card_id = item.cardId
            payModel.isSelected = item.isSelected ?: payModel.is_default == 1
            return payModel
        }
    }
}

data class CardModelNewItem(@SerializedName("card_id") var cardId: String? = "", @SerializedName("created_at") var createdAt: String? = "", @SerializedName(
    "id") var id: String? = "", @SerializedName("is_default") var isDefault: String? = "", @SerializedName(
    "last_four") var lastFour: String? = "", @SerializedName("name") var name: String? = "", @SerializedName(
    "updated_at") var updatedAt: String?, @SerializedName("user_id") var userId: String? = "",

                            var isSelected: Boolean? = false)