# Nebulosa API

## Auth

No authentication required.

## Connection

**POST** `/connect`

Query Params:

* `host`: INDI server host. `string` `required`
* `port`: INDI server port. `int` `required`

Request Body: `None`

Response Body: `200 None`

---

**POST** `/disconnect`

Query Params: `None`

Request Body: `None`

Response Body: `200 None`

---

**GET** `/connectionStatus`

Query Params: `None`

Request Body: `None`

Response Body: `200 application/json`

```json
true
```

---

## Camera

**GET** `/attachedCameras`

Query Params: `None`

Request Body: `None`

Response Body: `200 application/json`

```json
[
    {
        "name": "CCD Simulator",
        "connected": true,
        "exposuring": false,
        "hasCoolerControl": true,
        "coolerPower": 0.0,
        "cooler": false,
        "hasDewHeater": false,
        "dewHeater": false,
        "frameFormats": [
            "INDI_MONO"
        ],
        "canAbort": true,
        "cfaOffsetX": 0,
        "cfaOffsetY": 0,
        "cfaType": "RGGB",
        "exposureMin": 10000,
        "exposureMax": 3600000000,
        "exposureState": "IDLE",
        "exposure": 0,
        "hasCooler": true,
        "canSetTemperature": true,
        "canSubFrame": true,
        "x": 0,
        "minX": 0,
        "maxX": 0,
        "y": 0,
        "minY": 0,
        "maxY": 0,
        "width": 0,
        "minWidth": 0,
        "maxWidth": 0,
        "height": 0,
        "minHeight": 0,
        "maxHeight": 0,
        "canBin": true,
        "maxBinX": 4,
        "maxBinY": 4,
        "binX": 1,
        "binY": 1,
        "gain": 90,
        "gainMin": 0,
        "gainMax": 100,
        "offset": 0,
        "offsetMin": 0,
        "offsetMax": 6000,
        "hasGuiderHead": false,
        "pixelSizeX": 5.199999809265137,
        "pixelSizeY": 5.199999809265137,
        "canPulseGuide": true,
        "pulseGuiding": false,
        "hasThermometer": true,
        "temperature": 0.0
    }
]
```

---

**GET** `/camera`

Query Params:

* `name`: Name of the camera. `string` `required`

Request Body: `None`

Response Body: `200 application/json`

```json
{
    "name": "CCD Simulator",
    "connected": true,
    "exposuring": false,
    "hasCoolerControl": true,
    "coolerPower": 0.0,
    "cooler": false,
    "hasDewHeater": false,
    "dewHeater": false,
    "frameFormats": [
        "INDI_MONO"
    ],
    "canAbort": true,
    "cfaOffsetX": 0,
    "cfaOffsetY": 0,
    "cfaType": "RGGB",
    "exposureMin": 10000,
    "exposureMax": 3600000000,
    "exposureState": "IDLE",
    "exposure": 0,
    "hasCooler": true,
    "canSetTemperature": true,
    "canSubFrame": true,
    "x": 0,
    "minX": 0,
    "maxX": 0,
    "y": 0,
    "minY": 0,
    "maxY": 0,
    "width": 0,
    "minWidth": 0,
    "maxWidth": 0,
    "height": 0,
    "minHeight": 0,
    "maxHeight": 0,
    "canBin": true,
    "maxBinX": 4,
    "maxBinY": 4,
    "binX": 1,
    "binY": 1,
    "gain": 90,
    "gainMin": 0,
    "gainMax": 100,
    "offset": 0,
    "offsetMin": 0,
    "offsetMax": 6000,
    "hasGuiderHead": false,
    "pixelSizeX": 5.199999809265137,
    "pixelSizeY": 5.199999809265137,
    "canPulseGuide": true,
    "pulseGuiding": false,
    "hasThermometer": true,
    "temperature": 0.0
}
```

---

**POST** `/cameraConnect`

Query Params:

* `name`: Name of the camera. `string` `required`

Request Body: `None`

Response Body: `200 None`

---

**POST** `/cameraDisconnect`

Query Params:

* `name`: Name of the camera. `string` `required`

Request Body: `None`

Response Body: `200 None`

---

**GET** `/cameraIsCapturing`

Query Params:

* `name`: Name of the camera. `string` `required`

Request Body: `None`

Response Body: `200 application/json`

```json
true
```

---

**POST** `/cameraCooler`

Query Params:

* `name`: Name of the camera. `string` `required`
* `enable`: The cooler status. `boolean` `required`

Request Body: `None`

Response Body: `200 None`

---

**POST** `/cameraSetpointTemperature`

Query Params:

* `name`: Name of the camera. `string` `required`
* `temperature`: Setpoint temperature value. `double` `required`

Request Body: `None`

Response Body: `200 None`

---

**POST** `/cameraStartCapture`

Query Params:

* `name`: Name of the camera. `string` `required`

Request Body: `None`

Response Body: `200 None`

---

**POST** `/cameraAbortCapture`

Query Params:

* `name`: Name of the camera. `string` `required`

Request Body: `None`

Response Body: `200 None`

---

**PUT** `/cameraPreferences`

Query Params: None

Request Body: `application/json`

```json
{
    "autoSave": false,
    "savePath": "",
    "autoSubFolderMode": "OFF"
}
```

Response Body: `200 None`

---

**GET** `/cameraPreferences`

Query Params:

* `name`: Name of the camera. `string` `required`

Request Body: `None`

Response Body: `200 application/json`

```json
{
    "autoSave": false,
    "savePath": "",
    "autoSubFolderMode": "OFF"
}
```

---

## Image

**GET** `/imagesOfCamera`

Query Params:

* `name`: Name of the camera. `string` `required`

Request Body: `None`

Response Body: `200 application/json`

```json
[
    {
        "id": 1,
        "path": "/path/to/image.fits",
        "name": "CCD Simulator",
        "width": 1280,
        "height": 1024,
        "mono": true,
        "savedAt": 1234567890
    }
]
```

**GET** `/latestImageOfCamera`

Query Params:

* `name`: Name of the camera. `string` `required`

Request Body: `None`

Response Body: `200 application/json`

```json
{
    "id": 1,
    "path": "/path/to/image.fits",
    "name": "CCD Simulator",
    "width": 1280,
    "height": 1024,
    "mono": true,
    "savedAt": 1234567890
}
```

**GET** `/openImage`

Query Params:

* `path`: Image path. `string` `required`
* `debayer`: Enable auto STF. `boolean` `default: true`
* `autoStretch`: Enable auto STF. `boolean` `default: false`
* `shadow`: STF shadow value. `float` `min: 0.0` `max: 1.0` `default: 0.0`
* `highlight`: STF highlight value. `float` `min: 0.0` `max: 1.0`  `default: 1.0`
* `midtone`: STF midtone value. `float` `min: 0.0` `max: 1.0`  `default: 0.5`
* `mirrorHorizontal`: Flip the image horizontally. `boolean` `default: false`
* `mirrorVertical`: Flip the image vertically. `boolean` `default: false`
* `invert`: Invert the image. `boolean` `default: false`
* `scnrEnabled`: Enable SCNR. `boolean` `default: false`
* `scnrChannel`: SCNR channel. `enum` `default: GREEN` `values: RED, GREEN, BLUE`
* `scnrAmount`: SCNR amount. `float` `min: 0.0` `max: 1.0` `default: 0.5`
* `scnrProtectionMode`: SCNR protection method. `enum` `default: AVERAGE_NEUTRAL`

Request Body: `None`

Response Body: `200 image/png`

---

**POST** `/closeImage`

Query Params:

* `path`: Image path. `string` `required`

Request Body: `None`

Response Body: `200 None`

---

**GET** `/thumbnail`

Query Params:

* `id`: Id of the saved image. `int` `required`

Request Body: `None`

Response Body: `200 image/jpeg`

---

## INDI

**GET** `/indiProperties`

Query Params: `None`

Request Body: `None`

Response Body: `200 application/json`

```json
[
    {
        "name": "CONNECTION",
        "label": "Connection",
        "type": "SWITCH",
        "group": "Main Control",
        "perm": "RW",
        "state": "OK",
        "rule": "ONE_OF_MANY",
        "properties": [
            {
                "name": "CONNECT",
                "label": "Connect",
                "value": true
            },
            {
                "name": "DISCONNECT",
                "label": "Disconnect",
                "value": false
            }
        ]
    }
]
```

---

**POST** `/sendIndiProperty`

Query Params:

* `name`: Name of the device. `string` `required`

Request Body: `application/json`

```json
{
    "name": "CONNECTION",
    "type": "SWITCH",
    "properties": [
        {
            "name": "DISCONNECT",
            "value": true
        }
    ]
}
```

Response Body: `200 None`

---

## Atlas

**GET** `/locations`

Query Params: `None`

Request Body: `None`

Response Body: `200 application/json`

```json
[
    {
        "id": 1,
        "name": "City Name",
        "latitude": -1.0,
        "longitude": 4.0,
        "elevation": 8.0,
        "offsetInMinutes": -180
    }
]
```

---

**PUT** `/saveLocation`

Query Params:

* `id`: Id of the saved location or zero for be added. `int`

Request Body: `application/json`

```json
{
    "name": "City Name",
    "latitude": -1.0,
    "longitude": 4.0,
    "elevation": 8.0,
    "offsetInMinutes": -180
}
```

Response Body: `200 None`

---

**DELETE** `/deleteLocation`

Query Params:

* `id`: Id of the saved location. `int`

Request Body: `None`

Response Body: `200 None`

---

**GET** `/imageOfSun`

Query Params: `None`

Request Body: `None`

Response Body: `200 image/png`

---

**GET** `/imageOfMoon`

Query Params:

* `location`: Id of the location. `int` `required`
* `date`: Local Date. `string` `format: yyyy-MM-dd` `default: NOW`
* `time`: Local Time. `string` `format: HH:mm` `default: NOW`

Request Body: `None`

Response Body: `200 image/png`

---

**GET** `/positionOfSun`

Query Params:

* `location`: Id of the location. `int` `required`
* `date`: Local Date. `string` `format: yyyy-MM-dd` `default: NOW`
* `time`: Local Time. `string` `format: HH:mm` `default: NOW`

Request Body: `None`

Response Body: `200 application/json`

```json
{
    "rightAscensionJ2000": "07h10m10.0s",
    "declinationJ2000": "+022°27'26.1\"",
    "rightAscension": "07h11m32.7s",
    "declination": "+022°25'13.4\"",
    "azimuth": "286°00'20.4\"",
    "altitude": "-022°24'03.6\""
}
```

---

**GET** `/positionOfMoon`

Query Params:

* `location`: Id of the location. `int` `required`
* `date`: Local Date. `string` `format: yyyy-MM-dd` `default: NOW`
* `time`: Local Time. `string` `format: HH:mm` `default: NOW`

Request Body: `None`

Response Body: `200 application/json`

```json
{
    "rightAscensionJ2000": "07h10m10.0s",
    "declinationJ2000": "+022°27'26.1\"",
    "rightAscension": "07h11m32.7s",
    "declination": "+022°25'13.4\"",
    "azimuth": "286°00'20.4\"",
    "altitude": "-022°24'03.6\""
}
```

---

**GET** `/positionOfPlanet`

Query Params:

* `location`: Id of the location. `int` `required`
* `code`: JPL unique ID code. `string` `required`
* `date`: Local Date. `string` `format: yyyy-MM-dd` `default: NOW`
* `time`: Local Time. `string` `format: HH:mm` `default: NOW`

Request Body: `None`

Response Body: `200 application/json`

```json
{
    "rightAscensionJ2000": "07h10m10.0s",
    "declinationJ2000": "+022°27'26.1\"",
    "rightAscension": "07h11m32.7s",
    "declination": "+022°25'13.4\"",
    "azimuth": "286°00'20.4\"",
    "altitude": "-022°24'03.6\""
}
```

---

**GET** `/positionOfStar`

Query Params:

* `location`: Id of the location. `int` `required`
* `star`: Id of the star. `int` `required`
* `date`: Local Date. `string` `format: yyyy-MM-dd` `default: NOW`
* `time`: Local Time. `string` `format: HH:mm` `default: NOW`

Request Body: `None`

Response Body: `200 application/json`

```json
{
    "rightAscensionJ2000": "07h10m10.0s",
    "declinationJ2000": "+022°27'26.1\"",
    "rightAscension": "07h11m32.7s",
    "declination": "+022°25'13.4\"",
    "azimuth": "286°00'20.4\"",
    "altitude": "-022°24'03.6\""
}
```

---
**GET** `/positionOfDSO`

Query Params:

* `location`: Id of the location. `int` `required`
* `dso`: Id of the DSO. `int` `required`
* `date`: Local Date. `string` `format: yyyy-MM-dd` `default: NOW`
* `time`: Local Time. `string` `format: HH:mm` `default: NOW`

Request Body: `None`

Response Body: `200 application/json`

```json
{
    "rightAscensionJ2000": "07h10m10.0s",
    "declinationJ2000": "+022°27'26.1\"",
    "rightAscension": "07h11m32.7s",
    "declination": "+022°25'13.4\"",
    "azimuth": "286°00'20.4\"",
    "altitude": "-022°24'03.6\""
}
```

---

**GET** `/twilight`

Query Params:

* `location`: Id of the location. `int` `required`
* `date`: Local Date. `string` `format: yyyy-MM-dd` `default: NOW`

Request Body: `None`

Response Body: `200 application/json`

```json
{
    "civilDusk": [
        5.479528105482296,
        5.945870922970142
    ],
    "nauticalDusk": [
        5.945870922970142,
        6.412213740457989
    ],
    "astronomicalDusk": [
        6.412213740457989,
        6.878556557945836
    ],
    "night": [
        6.878556557945836,
        17.404580152671556
    ],
    "astronomicalDawn": [
        17.404580152671556,
        17.85426786953489
    ],
    "nauticalDawn": [
        17.85426786953489,
        18.32061068702279
    ],
    "civilDawn": [
        18.32061068702279,
        18.786953504510688
    ]
}
```

---

**GET** `/altitudePointsOfSun`

Query Params:

* `location`: Id of the location. `int` `required`
* `date`: Local Date. `string` `format: yyyy-MM-dd` `default: NOW`

Request Body: `None`

Response Body: `200 application/json`

```json
[
    [
        0.0,
        45.001459162
    ]
]
```

---

**GET** `/altitudePointsOfMoon`

Query Params:

* `location`: Id of the location. `int` `required`
* `date`: Local Date. `string` `format: yyyy-MM-dd` `default: NOW`

Request Body: `None`

Response Body: `200 application/json`

```json
[
    [
        0.0,
        -15.223360555
    ]
]
```

---

**GET** `/altitudePointsOfPlanet`

Query Params:

* `location`: Id of the location. `int` `required`
* `code`: JPL unique ID code. `string` `required`
* `date`: Local Date. `string` `format: yyyy-MM-dd` `default: NOW`

Request Body: `None`

Response Body: `200 application/json`

```json
[
    [
        0.0,
        -15.223360555
    ]
]
```

---

**GET** `/searchMinorPlanet`

Query Params:

* `text`: Text to search. `string` `required`

Request Body: `None`

Response Body: `200 application/json`

```json
{
    "found": true,
    "name": "253P/PANSTARRS",
    "spkId": 1003147,
    "kind": "cn",
    "pha": false,
    "neo": false,
    "orbitType": "JFc",
    "items": [
        {
            "orbital": true,
            "name": "e",
            "description": "eccentricity",
            "value": ".412586722616473",
            "unit": ""
        },
        {
            "orbital": false,
            "name": "M1",
            "description": "comet total magnitude",
            "value": "13.5",
            "unit": ""
        }
    ],
    "searchItems": []
}
```

---

**GET** `/searchStar`

Query Params:

* `text`: Text to search. `string` `required`

Request Body: `None`

Response Body: `200 application/json`

```json
[
    {
        "id": 32263,
        "hd": 48915,
        "hr": 2491,
        "hip": 32349,
        "names": "Sirius · Alp CMa · 9 CMa · HIP 32349 · HD 48915 · HR 2491",
        "magnitude": -1.46,
        "rightAscension": 1.7677953919253884,
        "declination": -0.2917512623453044,
        "type": "SPECTROSCOPIC_BINARY",
        "spType": "A0mA1Va",
        "redshift": -1.834585695059676E-5,
        "parallax": 1.8384619601354714E-6,
        "radialVelocity": -0.0031765158005019655,
        "distance": 8.600943479602332,
        "pmRA": -2.6471311802261778E-6,
        "pmDEC": -5.9296106895464015E-6,
        "constellation": "CMA"
    }
]
```

---

**GET** `/searchDSO`

Query Params:

* `text`: Text to search. `string` `required`

Request Body: `None`

Response Body: `200 application/json`

```json
[
    {
        "id": 255,
        "names": "Andromeda Galaxy · Andromeda Nebula · Great Nebula in Andromeda · Great Spiral · Andromeda A · NGC 224 · M 31 · PGC 2557 · UGC 454",
        "m": 31,
        "ngc": 224,
        "ic": 0,
        "c": 0,
        "b": 0,
        "sh2": 0,
        "vdb": 0,
        "rcw": 0,
        "ldn": 0,
        "lbn": 0,
        "cr": 0,
        "mel": 0,
        "pgc": 2557,
        "ugc": 454,
        "arp": 0,
        "vv": 0,
        "dwb": 0,
        "tr": 0,
        "st": 0,
        "ru": 0,
        "vdbha": 0,
        "ced": null,
        "pk": null,
        "png": null,
        "snrg": null,
        "aco": null,
        "hcg": null,
        "eso": null,
        "vdbh": null,
        "magnitude": 3.4000000953674316,
        "rightAscension": 0.18648332357406616,
        "declination": 0.7202755212783813,
        "type": "GALAXY",
        "redshift": -0.0010001920937326991,
        "parallax": 2.9088820866572163E-8,
        "radialVelocity": -0.17326449820919812,
        "distance": 2537496.6184282,
        "majorAxis": 0.058040924275071425,
        "minorAxis": 0.02059197629144643,
        "orientation": 0.7853981633974483,
        "pmRA": 0.0,
        "pmDEC": 0.0,
        "constellation": "AND",
        "mtype": "SA(s)b"
    }
]
```

---
