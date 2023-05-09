# SmartGlassesDriveAssistant

This is an example app that displays your vehicle's speed and engine RPM on your smartglasses. It works in conjunction with the [SmartGlassesManager](https://github.com/TeamOpenSmartGlasses/SmartGlassesManager).

[![Demo video](http://img.youtube.com/vi/XDQ6MuoZ7AE/0.jpg)](https://www.youtube.com/watch?v=XDQ6MuoZ7AE "SmartGlassesDriveAssistant Demo")

## Installation

* Install the [SmartGlassesManager](https://github.com/TeamOpenSmartGlasses/SmartGlassesManager) on your phone and make sure it's running.

* Install this app on your phone.

## Usage

This app gets your vehicle's speed and RPM from a Bluetooth ODB II reader. You can buy one for $13 [here](https://www.amazon.com/dp/B09VXDBL8G?psc=1&ref=ppx_yo2ov_dt_b_product_details), but almost all other ELM327-based ODB II readers should work.

* Plug the OBD II reader into your vehicle

* Pair it with your phone in your Bluetooth settings

* Launch the app via the [SmartGlassesManager](https://github.com/TeamOpenSmartGlasses/SmartGlassesManager)
* * (Do this by saying: "Hey computer, drive assistant")

## TODO

* [ ] Optimize display refresh
    * Only refresh for significant changes (±50 rpm, ±1mph)
    * Prevent "flicker" on display update (related to SGM)
* [ ] Improve padding
    * Make left padding work
    * Add user-configurable option to enable top and/or left padding in the app
* [ ] Stretch goals
    * Google Maps (or similar) integration for navigation
    * [RoadCurvature](https://roadcurvature.com/) integration for blind turns

## Contributing

Pull requests are welcome. For major changes, please open an issue first
to discuss what you would like to change.

## License

[MIT](https://github.com/TeamOpenSmartGlasses/SmartGlassesDriveAssistant/blob/main/LICENSE)
