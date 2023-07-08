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

**GET** `/image`

Query Params:

* `hash`: Hex-encoded path of an image. `string` `required`
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
