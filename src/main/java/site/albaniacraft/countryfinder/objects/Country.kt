package site.albaniacraft.countryfinder.objects

data class Country(
    val id: Int,
    val name: String,
    val cities: List<City>,
    val border: List<Coordinate>
)