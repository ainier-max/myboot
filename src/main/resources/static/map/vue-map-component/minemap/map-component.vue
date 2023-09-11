<template>
  <div :id="id" ref="mapComponent">

  </div>
</template>

<script>
    module.exports = {
        name: "MineMapComponent",
        props: {
            id: {type: String, default: ''},
            type: {type: String, default: ''},
            config: {type: Object, default: () => ({})},
        },
        data: function () {
            return {
                map: null
            }
        },
        methods: {
            loadMap() {
                if (typeof (window.minemap) != "undefined" && this.id != "") {
                    var mapJson = this.config;//该值从mapConfig.js获取
                    mapJson.id = this.id;
                    mapJson.useType = this.type; //调用使用类型[vue,js],必须指定
                    this.initMap(mapJson);
                } else {
                    console.log("window.minemap", window.minemap);
                    window.setTimeout(this.loadMap, 10);
                }
            },
            initMap(mapJson) {
                console.log("地图初始配置参数12：", mapJson);
                minemap.domainUrl = mapJson.domainUrl;
                minemap.dataDomainUrl = mapJson.dataDomainUrl;
                minemap.spriteUrl = mapJson.spriteUrl;
                minemap.fontsUrl = mapJson.fontsUrl;
                minemap.serviceUrl = mapJson.serviceUrl;
                minemap.accessToken = mapJson.accessToken;
                minemap.solution = mapJson.solution;
                this.map = new minemap.Map({
                    container: mapJson.id,
                    style: mapJson.mapStyle,
                    center: mapJson.center,
                    zoom: mapJson.zoom,
                    pitch: 0
                });
                //为地图对象添加useType赋值
                this.map.useType = mapJson.useType;
                //为地图对象添加mapType赋值
                this.map.mapType = "minemap";
                this.useType = mapJson.useType;
                //$(".minemap-popup-close-button").css("color","black");
                //$(".minemap-popup-content").css("color","black");
            },
            addZoomControl: function (position) {
                console.log("四维图新暂不支持缩放控件");
            },
            addPanControl: function (position) {
                console.log("四维图新暂不支持平移控件");
            },
            /**
             * 移除缩放控件
             */
            removeZoomControl: function (object) {
                console.log("四维图新暂不支持缩放控件");
            },
            /**
             * 移除平移控件
             */
            removePanControl: function (object) {
                console.log("四维图新暂不支持平移控件");
            },
            addMarker: function (markerJSON) {
                console.log("markerJSON:", markerJSON);
                if (markerJSON.xy) {

                } else {
                    console.log("坐标异常,其值为", markerJSON.xy);
                    return;
                }
                var lat = markerJSON.xy[0];
                var lng = markerJSON.xy[1];
                var newXY = [lng, lat];

                if (markerJSON.iconAnchor) {

                } else {
                    console.log("无坐标偏移,其值不能为空,", markerJSON.iconAnchor);
                    return;
                }
                var iconAnchorWidth = markerJSON.iconAnchor[0];
                var iconAnchorHeight = markerJSON.iconAnchor[1];

                //console.log("markerJSON参数值:",markerJSON);
                var el = document.createElement('div');

                var myDate = new Date();
                var randomTemp = Math.floor(Math.random() * 100000000);
                var markerIDRandom = "marker" + myDate.getTime() + "ID" + randomTemp;

                el.id = markerIDRandom;
                el.style["background-image"] = "url(\'" + markerJSON.iconUrl + "\')";
                el.style["background-size"] = "cover";
                el.style.width = markerJSON.width + "px";
                el.style.height = markerJSON.height + "px";
                el.style["border-radius"] = "50%";
                el.style.cursor = "pointer";
                var marker = new minemap.Marker(el, {offset: [-iconAnchorWidth, -iconAnchorHeight]})
                    .setLngLat(newXY);
                this.map.addMarker(marker);
                marker.id = markerIDRandom;
                marker.type = "marker";
                marker.layerType = "marker";
                return marker;
            },
            /**
             * 画线
             **/
            addPolyline: function (polylineJSON) {
                var myDate = new Date();
                var randomTemp = Math.floor(Math.random() * 100000000);
                var polylineSourceRandom = "polyline" + myDate.getTime() + "source" + randomTemp;
                var polylineIDRandom = "polyline" + myDate.getTime() + "ID" + randomTemp;
                //console.log("polygonSourceRandom随机数",polygonSourceRandom);
                //console.log("polygonIDRandom随机数",polygonIDRandom);
                polylineJSON.newXYS = [];
                for (var i = 0; i < polylineJSON.xys.length; i++) {
                    var y = polylineJSON.xys[i][0];
                    var x = polylineJSON.xys[i][1];
                    polylineJSON.newXYS.push([x, y]);
                }
                console.log("更新后的坐标数据:", polylineJSON.newXYS);
                this.map.addSource(polylineSourceRandom, {
                    'type': 'geojson',
                    'data': {
                        'type': 'Feature',
                        'geometry': {
                            'type': 'LineString',
                            'coordinates': polylineJSON.newXYS
                        }
                    }
                });

                var polylineTemp = {
                    'id': polylineIDRandom,
                    'type': 'line',
                    'source': polylineSourceRandom,
                    'layout': {},
                    'paint': {
                        "line-color": polylineJSON.option.color,
                        "line-width": polylineJSON.option.weight
                    }
                };

                this.map.addLayer(polylineTemp);
                var polyline = this.map.getLayer(polylineIDRandom);
                polyline.id = polylineIDRandom;
                polyline.layerType = "polyline";
                polyline.zbc = polylineJSON.newXYS;
                console.log("叠加的线对象:", polyline);
                return polyline;
            },
            /**
             * 画面
             **/
            addPolygon: function (polygonJSON) {
                var myDate = new Date();
                var randomTemp = Math.floor(Math.random() * 100000000);
                var polygonSourceRandom = "polygon" + myDate.getTime() + "source" + randomTemp;
                var polygonIDRandom = "polygon" + myDate.getTime() + "ID" + randomTemp;
                //console.log("polygonSourceRandom随机数",polygonSourceRandom);
                //console.log("polygonIDRandom随机数",polygonIDRandom);
                polygonJSON.newXYS = [[]];
                for (var i = 0; i < polygonJSON.xys[0].length; i++) {
                    var y = polygonJSON.xys[0][i][0];
                    var x = polygonJSON.xys[0][i][1];
                    polygonJSON.newXYS[0].push([x, y]);
                }

                console.log("面，更新后的坐标数据:", polygonJSON.newXYS);
                this.map.addSource(polygonSourceRandom, {
                    'type': 'geojson',
                    'data': {
                        'type': 'Feature',
                        'geometry': {
                            'type': 'Polygon',
                            'coordinates': polygonJSON.newXYS
                        }
                    }
                });

                var polygonTemp = {
                    'id': polygonIDRandom,
                    'type': 'fill',
                    'source': polygonSourceRandom,
                    'layout': {"visibility": "visible"},
                    'paint': {
                        'fill-outline-color': polygonJSON.option.color,
                        'fill-color': polygonJSON.option.fillColor,
                        'fill-opacity': polygonJSON.option.fillOpacity
                    }
                };

                this.map.addLayer(polygonTemp);
                var polygon = this.map.getLayer(polygonIDRandom);
                polygon.layerType = "polygon";
                polygon.id = polygonIDRandom;
                polygon.zbc = polygonJSON.xys;
                var layerBound = this.getLayerBound(polygon);
                polygon.centerPoint = {};
                polygon.centerPoint.lat = (layerBound.south + layerBound.north) / 2;
                polygon.centerPoint.lng = (layerBound.east + layerBound.west) / 2;
                //console.log("叠加的面对象:",polygon);
                return polygon;
            },

            /**
             * 画矩形
             **/
            addRect: function (rectJson) {
                var myDate = new Date();
                var randomTemp = Math.floor(Math.random() * 100000000);
                var rectSourceRandom = "rect" + myDate.getTime() + "source" + randomTemp;
                var rectIDRandom = "rect" + myDate.getTime() + "ID" + randomTemp;
                //console.log("polygonSourceRandom随机数",polygonSourceRandom);
                //console.log("polygonIDRandom随机数",polygonIDRandom);
                console.log("rectJson:", rectJson);
                rectJson.newXYS = [[]];
                for (var i = 0; i < rectJson.xys.length; i++) {
                    var y = rectJson.xys[i][0];
                    var x = rectJson.xys[i][1];
                    rectJson.newXYS[0].push([x, y]);
                }
                rectJson.newXYS[0].push(rectJson.newXYS[0][0]);
                console.log("矩形，更新后的坐标数据:", rectJson.newXYS);
                this.map.addSource(rectSourceRandom, {
                    'type': 'geojson',
                    'data': {
                        'type': 'Feature',
                        'geometry': {
                            'type': 'Polygon',
                            'coordinates': rectJson.newXYS
                        }
                    }
                });

                var rectTemp = {
                    'id': rectIDRandom,
                    'type': 'fill',
                    'source': rectSourceRandom,
                    'layout': {},
                    'paint': {
                        'fill-outline-color': rectJson.option.color,
                        'fill-color': rectJson.option.fillColor,
                        'fill-opacity': rectJson.option.fillOpacity
                    }
                };

                this.map.addLayer(rectTemp);
                var rect = this.map.getLayer(rectIDRandom);
                rect.layerType = "rect";
                rect.id = rectIDRandom;
                rect.zbc = rectJson.xys;
                var layerBound = this.getLayerBound(rect);
                rect.centerPoint = {};
                rect.centerPoint.lat = (layerBound.south + layerBound.north) / 2;
                rect.centerPoint.lng = (layerBound.east + layerBound.west) / 2;
                //console.log("叠加的面对象:",polygon);
                return rect;
            },

            /**
             * 画圆
             */
            addCircle(circleJson) {
                var center = [circleJson.xy[1], circleJson.xy[0]];
                var radius = circleJson.option.radius / 1000;
                var options = {steps: 400, units: 'kilometers', properties: {foo: 'bar'}};
                var cirlceData = turf.circle(center, radius, options);
                //console.log("cirlceData:",cirlceData);

                var myDate = new Date();
                var randomTemp = Math.floor(Math.random() * 100000000);
                var circleSourceRandom = "circle" + myDate.getTime() + "source" + randomTemp;
                var circleIDRandom = "circle" + myDate.getTime() + "ID" + randomTemp;

                this.map.addSource(circleSourceRandom, {
                    "type": "geojson",
                    "data": cirlceData
                });

                this.map.addLayer({
                    "id": circleIDRandom,
                    "type": "fill",
                    "source": circleSourceRandom,
                    "layout": {},
                    "paint": {
                        'fill-outline-color': circleJson.option.color,
                        'fill-color': circleJson.option.fillColor,
                        'fill-opacity': circleJson.option.fillOpacity
                    }
                });

                var circleLayyer = this.map.getLayer(circleIDRandom);
                circleLayyer.layerType = "circle";
                circleLayyer.id = circleIDRandom;
                circleLayyer.centerPoint = {};
                circleLayyer.centerPoint.lat = center[1];
                circleLayyer.centerPoint.lng = center[0];
                circleLayyer.radius = circleJson.option.radius;
                return circleLayyer;

            },

            addPolygonByGeojson(areaBoundaryPoints, option) {
                var geometryObj = JSON.parse(areaBoundaryPoints);
                console.log("geometryObj:", geometryObj);
                var myDate = new Date();
                var randomTemp = Math.floor(Math.random() * 100000000);
                var polygonSourceRandom = "polygon" + myDate.getTime() + "source" + randomTemp;
                var polygonIDRandom = "polygon" + myDate.getTime() + "ID" + randomTemp;

                this.map.addSource(polygonSourceRandom, {
                    'type': 'geojson',
                    'data': {
                        'type': 'Feature',
                        'geometry': geometryObj
                    }
                });
                var polygonTemp = {
                    'id': polygonIDRandom,
                    'type': 'fill',
                    'source': polygonSourceRandom,
                    'layout': {},
                    'paint': {
                        'fill-color': "#f18",
                        'fill-opacity': 0.6
                    }
                };

                this.map.addLayer(polygonTemp);
                var layer = this.map.getLayer(polygonIDRandom);
                layer.layerType = "polygon";
                layer.id = polygonIDRandom;
                layer.zbc = geometryObj.coordinates[0];
                var layerBound = this.getLayerBound(layer);
                layer.centerPoint = {};
                layer.centerPoint.lat = (layerBound.south + layerBound.north) / 2;
                layer.centerPoint.lng = (layerBound.east + layerBound.west) / 2;
                return layer;
            },

            /**
             * 两点之间的距离，单位米，存在些微误差
             */
            distace(point1, point2) {
                var xdi = point2[1] - point1[1];
                var ydi = point2[0] - point1[0];
                var degree = Math.pow((xdi * xdi + ydi * ydi), 0.5);
                var meter = (1000 * 101.11) * degree;
                return meter;
            },
            /**
             * 米转度
             * @param meter
             */
            meterToDegree(meter) {
                var degree = meter / (1000 * 101.11);
                return degree;
            },
            /**
             * 度转米
             * @param meter
             */
            degreeToMeter(degree) {
                var meter = (1000 * 101.11) * degree;
                return meter;
            },


            /**
             * 画div方法（json格式）
             * divJson.point坐标串数据
             * divJson.color线的颜色
             * divJson.content气泡框内容
             */
            addDiv(divJson) {
                var el = document.createElement('div');
                console.log(divJson.option.html);
                el.innerHTML = divJson.option.html;
                var anchor = [-divJson.option.iconAnchor[0], -divJson.option.iconAnchor[1]];
                var divLayer = new minemap.Marker(el, {offset: anchor}).setLngLat([divJson.xy[1], divJson.xy[0]]).addTo(this.map);
                divLayer.type = "div";
                return divLayer;
            },

            /**
             * 点，线，面绑定气泡框
             */
            bindPopup: function (object, option) {
                //console.log("this,bindPopup:",this);
                //console.log("元素对象:",object);
                if (object.type == "marker") {
                    console.log("气泡框对象类型typeof", typeof (object.html));
                    if (typeof (object.html) == "object") {
                        var popupTemp = new minemap.Popup()
                            .setDOMContent(object.html);
                        object.setPopup(popupTemp);
                    } else {
                        var popupTemp = new minemap.Popup()
                            .setHTML(object.html);
                        object.setPopup(popupTemp);
                    }
                } else {
                    window.cbcObject = object;
                    window.cbcHtml = object.html;
                    if (window.cbcPopupArr) {
                        window.cbcPopupArr.push(object.id);
                    } else {
                        window.cbcPopupArr = [];
                        window.cbcPopupArr.push(object.id);
                    }
                    if (window.cbcPopup) {

                    } else {
                        window.cbcPopup = new minemap.Popup({
                            closeButton: true,
                            closeOnClick: false
                        });
                    }
                    this.map.off('click', this.onPopupClick);
                    window.cbcMineMap = this.map;
                    this.map.on('click', this.onPopupClick);
                }
            },

            onPopupClick: function (e) {
                //console.log("the,onPopupClick:",the);
                //console.log(e.point);
                //console.log("window.cbcPopupArr:",window.cbcPopupArr);
                var features = window.cbcMineMap.queryRenderedFeatures(e.point, {layers: window.cbcPopupArr});
                //console.log("features123:",features);
                for (var i = 0; i < features.length; i++) {
                    var mapLayer = window.cbcMineMap.getLayer(features[i].layer.id);
                    //console.log("mapObject:",mapLayer);
                    window.cbcPopup.setLngLat(e.lngLat)
                        .setHTML(mapLayer.html)
                        .addTo(window.cbcMineMap);
                }
            },
            /**
             * 地图添加气泡框
             */
            addPopup: function (option, point, html) {
                var popup = new minemap.Popup(option)
                    .setLngLat([point[1], point[0]])
                    .setHTML(html)
                    .addTo(this.map);
                popup.type = "popup";
                return popup;
            },

            /**
             *  中心点居中
             */
            panTo: function (point) {
                if (point) {
                    var lat = point[0];
                    var lng = point[1];
                    this.map.panTo([lng, lat]);
                }
            },
            /**
             *  级别缩放
             */
            zoomTo: function (level) {
                this.map.zoomTo(level);
            },
            /**
             * 缩放并居中
             */
            setView: function (point, level) {
                if (point) {
                    var lat = point[0];
                    var lng = point[1];
                    this.map.setZoomAndCenter(level, [lng, lat]);
                }

            },

            /**
             * 删除图层
             */
            removeLayer: function (layer) {
                if (typeof (layer) != "undefined" && (layer.type == "marker" || layer.type == "popup" || layer.type == "div")) {
                    layer.remove();
                } else {
                    if (window.cbcPopupArr) {
                        for (var i = 0; i < window.cbcPopupArr.length; i++) {
                            if (window.cbcPopupArr[i] == layer.id) {
                                window.cbcPopupArr.splice(i, 1);
                                i = i - 1;
                            }
                        }
                    }
                    this.map.removeLayer(layer.id);
                    this.map.removeSource(layer.source);
                }
            },

            setIcon: function (layer, iconJson) {
                console.log("四维图新不支持图标切换");
            },

            /**
             * 样式切换
             */
            setStyle: function (layer, option) {
                if ("polyline" == layer.layerType) {
                    if (option.color) {
                        this.map.setPaintProperty(layer.id, 'line-color', option.color);
                    }
                    if (option.weight) {
                        this.map.setPaintProperty(layer.id, 'line-width', option.weight);
                    }
                }

                if ("polygon" == layer.layerType) {
                    if (option.fillColor) {
                        this.map.setPaintProperty(layer.id, 'fill-outline-color', option.fillColor);
                    }
                    if (option.color) {
                        this.map.setPaintProperty(layer.id, 'fill-color', option.color);
                    }
                    if (option.fillOpacity) {
                        this.map.setPaintProperty(layer.id, 'fill-opacity', option.fillOpacity);
                    }
                }

                if ("circle" == layer.layerType) {
                    if (option.fillColor) {
                        this.map.setPaintProperty(layer.id, 'fill-outline-color', option.fillColor);
                    }
                    if (option.color) {
                        this.map.setPaintProperty(layer.id, 'fill-color', option.color);
                    }
                    if (option.fillOpacity) {
                        this.map.setPaintProperty(layer.id, 'fill-opacity', option.fillOpacity);
                    }
                }

                if ("rect" == layer.layerType) {
                    if (option.fillColor) {
                        this.map.setPaintProperty(layer.id, 'fill-outline-color', option.fillColor);
                    }
                    if (option.color) {
                        this.map.setPaintProperty(layer.id, 'fill-color', option.color);
                    }
                    if (option.fillOpacity) {
                        this.map.setPaintProperty(layer.id, 'fill-opacity', option.fillOpacity);
                    }
                }
            },

            /**
             * 获取地图中心点
             */
            getMapCenter: function () {
                return this.map.getCenter();
            },
            /**
             * 获取当前地图级别
             */
            getMapZoom: function () {
                return this.map.getZoom();
            },
            /**
             * 获取地图边框
             */
            getMapBound: function () {
                var bound = {};
                bound.east = this.map.getBounds().getEast();
                bound.west = this.map.getBounds().getWest();
                bound.south = this.map.getBounds().getSouth();
                bound.north = this.map.getBounds().getNorth();
                return bound;
            },
            /**
             * 获取图层边框
             */
            getLayerBound: function (layerObject) {
                //console.log("layerObject:",layerObject);
                var xArr = [];
                var yArr = [];
                if (layerObject.layerType == "polygon") {
                    //console.log("eee:",layerObject.zbc[0][0][0]);
                    if (typeof (layerObject.zbc[0][0][0]) != "undefined") {
                        for (var i = 0; i < layerObject.zbc[0].length; i++) {
                            xArr.push(layerObject.zbc[0][i][0]);
                            yArr.push(layerObject.zbc[0][i][1]);
                        }
                    } else {
                        for (var i = 0; i < layerObject.zbc.length; i++) {
                            xArr.push(layerObject.zbc[i][0]);
                            yArr.push(layerObject.zbc[i][1]);
                        }
                    }

                }

                if (layerObject.layerType == "rect") {
                    for (var i = 0; i < layerObject.zbc[0].length; i++) {
                        xArr.push(layerObject.zbc[0][i][0]);
                        yArr.push(layerObject.zbc[0][i][1]);
                    }
                }

                if (layerObject.layerType == "polyline") {
                    for (var i = 0; i < layerObject.zbc.length; i++) {
                        xArr.push(layerObject.zbc[i][0]);
                        yArr.push(layerObject.zbc[i][1]);
                    }
                }

                if (layerObject.layerType == "circle") {
                    var degree = this.meterToDegree(layerObject.radius);
                    xArr.push(layerObject.center[0] + degree);
                    xArr.push(layerObject.center[0] - degree);
                    yArr.push(layerObject.center[1] + degree);
                    yArr.push(layerObject.center[1] - degree);
                }

                //console.log("xArr:",xArr);
                //console.log("yArr:",yArr);
                var east = Math.max.apply(null, xArr);
                var west = Math.min.apply(null, xArr);
                var north = Math.max.apply(null, yArr);
                var south = Math.min.apply(null, yArr);

                var bound = {};
                bound.east = east;
                bound.west = west;
                bound.south = south;
                bound.north = north;
                return bound;
            },

            /**
             * 根据图层边框居中
             */
            setCenterByBound: function (boundObject) {
                var bounds = [[boundObject.west, boundObject.south], [boundObject.east, boundObject.north]];//边界框
                this.map.fitBounds(bounds);
            },


            /**
             * 地图事件
             */
            //地图绑定点击事件
            onClick: function (callback) {
                this.map.on("click", callback);
            },
            //地图移除点击事件
            offClick: function (callback) {
                this.map.off("click", callback);
            },
            //地图绑定移动开始事件
            onMoveStart: function (callback) {
                this.map.on("movestart", callback);
            },
            //地图绑定移动中事件
            onMove: function (callback) {
                this.map.on("move", callback);
            },
            //地图绑定移动结束事件
            onMoveEnd: function (callback) {
                this.map.on("moveend", callback);
            },
            //地图移除移动开始事件
            offMoveStart: function (callback) {
                this.map.off("movestart", callback);
            },
            //地图移除移动中事件
            offMove: function (callback) {
                this.map.off("move", callback);
            },
            //地图移除移动结束事件
            offMoveEnd: function (callback) {
                this.map.off("moveend", callback);
            },
            //地图绑定缩放开始事件
            onZoomStart: function (callback) {
                this.map.on("zoomstart", callback);
            },
            //地图绑定缩放中事件
            onZoom: function (callback) {
                this.map.on("zoom", callback);
            },
            //地图绑定缩放结束事件
            onZoomEnd: function (callback) {
                this.map.on("zoomend", callback);
            },
            //地图移除缩放开始事件
            offZoomStart: function (callback) {
                this.map.off("zoomstart", callback);
            },
            //地图移除缩放中事件
            offZoom: function (callback) {
                this.map.off("zoom", callback);
            },
            //地图移除缩放结束事件
            offZoomEnd: function (callback) {
                this.map.off("zoomend", callback);
            },


            initEdit: function () {
                //console.log(this.map.editObj);
                if (!this.map.editObj) {
                    this.map.editObj = new minemap.edit.init(this.map, {
                        boxSelect: false,
                        touchEnabled: true,
                        displayControlsDefault: true,
                        showButtons: false
                    });
                }
            },

            drawMarker: function (markerJSON, dragFlag, callback) {
                window.clearMapEditFlag = 0;
                var the = this;
                this.initEdit();
                this.map.editObj.onBtnCtrlActive("point");
                this.map.once("edit.record.create", function (e) {
                    console.log("采集返回的值1：", e);
                    if (window.clearMapEditFlag == 0) {
                        the.map.editObj.draw.deleteAll();
                        var gather_xy = e.record.features[0].geometry.coordinates;
                        markerJSON.xy = [gather_xy[1], gather_xy[0]];
                        var gatherMarker = the.addMarker(markerJSON);
                        gatherMarker.xy = {"lat": gather_xy[1], "lng": gather_xy[0]};
                        callback(gatherMarker);
                    } else {
                        the.map.editObj.draw.deleteAll();
                        console.log("地图取消编辑状态");
                    }

                });
            },

            drawPolyline: function (callback) {
                window.clearMapEditFlag = 0;
                var the = this;
                this.initEdit();
                this.map.editObj.onBtnCtrlActive("line");
                this.map.once("edit.record.create", function (e) {
                    console.log("采集返回的值：", e);
                    if (window.clearMapEditFlag == 0) {
                        the.map.editObj.draw.deleteAll();
                        var xys = e.record.features[0].geometry.coordinates;
                        console.log("采集返回的值xys：", xys);
                        var zbc = [];
                        var newXYS = [];
                        for (var i = 0; i < xys.length; i++) {
                            var obj = {};
                            obj.lat = xys[i][1];
                            obj.lng = xys[i][0];
                            zbc.push(obj);
                            newXYS.push([obj.lat, obj.lng]);
                        }
                        console.log("采集返回的值newXYS：", newXYS);
                        var polylineJSON = {};
                        polylineJSON.xys = newXYS;
                        polylineJSON.option = {};
                        polylineJSON.option.weight = 5;
                        polylineJSON.option.color = "#3388ff";
                        var polyline = the.addPolyline(polylineJSON);
                        polyline.zbc = zbc;
                        callback(polyline);
                    } else {
                        the.map.editObj.draw.deleteAll();
                        console.log("地图取消编辑状态");
                    }

                });
            },

            drawPolygon: function (callback) {
                window.clearMapEditFlag = 0;
                var the = this;
                this.initEdit();
                this.map.editObj.onBtnCtrlActive("polygon");
                this.map.once("edit.record.create", function (e) {
                    if (window.clearMapEditFlag == 0) {
                        the.map.editObj.draw.deleteAll();
                        var xys = e.record.features[0].geometry.coordinates;
                        var zbc = [];
                        var newXYS = [];
                        for (var i = 0; i < xys[0].length; i++) {
                            var obj = {};
                            obj.lat = xys[0][i][1];
                            obj.lng = xys[0][i][0];
                            zbc.push(obj);
                            newXYS.push([obj.lat, obj.lng]);
                        }
                        var polygonJSON = {};
                        polygonJSON.xys = [newXYS];
                        //面的样式
                        polygonJSON.option = {};
                        //边界线大小(四维图形接口宽度不生效)
                        polygonJSON.option.weight = 5;
                        //边界线颜色
                        polygonJSON.option.color = "#3388ff";
                        //填充颜色
                        polygonJSON.option.fillColor = "#3388ff";
                        //透明度
                        polygonJSON.option.fillOpacity = 0.5;
                        var polygon = the.addPolygon(polygonJSON);
                        polygon.zbc = zbc;
                        callback(polygon);
                    } else {
                        the.map.editObj.draw.deleteAll();
                        console.log("地图取消编辑状态");
                    }

                });
            },

            drawRect: function (callback) {
                window.clearMapEditFlag = 0;
                var the = this;
                this.initEdit();
                this.map.editObj.onBtnCtrlActive("rectangle");
                this.map.once("edit.record.create", function (e) {
                    if (window.clearMapEditFlag == 0) {
                        the.map.editObj.draw.deleteAll();
                        var xys = e.record.features[0].geometry.coordinates;
                        console.log("采集的坐标串12：", xys[0]);
                        var zbc = [];
                        var newXYS = [];
                        for (var i = 0; i < xys[0].length; i++) {
                            var obj = {};
                            obj.lat = xys[0][i][1];
                            obj.lng = xys[0][i][0];
                            if (i < (xys[0].length - 1)) {
                                zbc.push(obj);
                                newXYS.push([obj.lat, obj.lng]);
                            }
                        }
                        var rectJson = {};
                        rectJson.xys = newXYS;
                        rectJson.option = {};
                        rectJson.option.weight = 5;
                        rectJson.option.color = "#3388ff";
                        rectJson.option.fillColor = "#3388ff";
                        rectJson.option.fillOpacity = 0.5;
                        var rect = the.addRect(rectJson);
                        rect.zbc = zbc;
                        callback(rect);
                    } else {
                        the.map.editObj.draw.deleteAll();
                        console.log("地图取消编辑状态");
                    }

                });
            },
            /**
             * 采集圆
             */
            drawCircle: function (callback) {
                window.clearMapEditFlag = 0;
                var the = this;
                this.initEdit();
                this.map.editObj.onBtnCtrlActive("circle");
                this.map.once("edit.record.create", function (e) {
                    if (window.clearMapEditFlag == 0) {
                        the.map.editObj.draw.deleteAll();
                        //var xys=e.record.features[0].geometry.coordinates;
                        console.log("采集的坐标串12：", e.record.features[0].properties);
                        var centerPoint = [e.record.features[0].properties.center[1], e.record.features[0].properties.center[0]];
                        var radius = e.record.features[0].properties.radius * 1000;

                        var circleJson = {};
                        circleJson.xy = centerPoint;
                        circleJson.option = {};
                        circleJson.option.radius = radius;//单位米
                        circleJson.option.color = "#3388ff";//ff7800
                        circleJson.option.fillColor = "#3388ff";
                        circleJson.option.fillOpacity = 0.5;
                        var circle = the.addCircle(circleJson);
                        circle.centerPoint = {};
                        circle.centerPoint.lat = centerPoint[0];
                        circle.centerPoint.lng = centerPoint[1];
                        circle.radius = radius;
                        callback(circle);
                    } else {
                        the.map.editObj.draw.deleteAll();
                        console.log("地图取消编辑状态");
                    }

                });
            }


        },
        mounted() {
            this.loadMap();
        }
    }

</script>

<style>

</style>
