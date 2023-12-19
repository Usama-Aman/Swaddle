package com.android.swaddle.listeners

interface ItemClickListener {
    fun itemTapped(position: Int)
}

interface SideMenuItemClickedListener {

    fun sideMenuItemTapped(position: Int, type: String)
}

interface OrganizationDetailInterface {
    fun goToOrganizationDetail(position: Int, id: Int, name: String)
    fun likeNGO(position: Int, id: Int)
    fun unlikeNgo(position: Int, id: Int)
}

interface CampaignDetailInterface {
    fun goToCampaignDetail(position: Int, id: Int, name: String)
}

interface DonorDetailInterface {
    fun goToDonorDetail(position: Int, id: Int)
}

interface CardInterface {
    fun removeCard(position: Int, id: Int, is_default: Int)
    fun setAsDefault(
        position: Int,
        id: Int,
        is_default: Boolean
    )

    fun getSelectedCard(position: Int, cardId: Int)
}

interface MediaInterface {
    fun deleteMedia(position: Int, id: Int)
}

interface CountryData {
    fun getSelectedCountryData(position: Int, countryCode: String)

}
interface FrequentDonation{
    fun stopFrequentDonation(position: Int , id : Int , status : Boolean)
}
