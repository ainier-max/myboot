<template>
    <!--class="mapClass"-->
    <div :id="id" ref="mapComponent">

    </div>
    <!-- export default { -->
</template>

<script>

    module.exports = {
        name: "LeafletMapComponent",
        props: {
            id: {type: String, default: ''},
            type: {type: String, default: ''},
            config: {type: Object, default: () => ({})},
            initPosition: {type: Object, default: () => ({})},
            loadEnd: {type: String},
        },
        data: function () {
            return {
                map: null,//地图对象
                outPolylineData: null,
                inPolylineData: null,
                arrInfo: [],
                //下面两个参数供迁徙图地图缩放使用
                geoMapData: "",
                geoMapTileLayer: null
            }
        },
        methods: {
            loadMap() {
                if (typeof (window.L) != "undefined" && typeof (window.L.Proj) != "undefined" && this.id != "" && typeof (document.getElementById(this.id)) != "undefined") {
                    console.log("地图id值", this.id);
                    var mapJson = this.config;
                    mapJson.id = this.id;
                    mapJson.useType = this.type;
                    this.initMap(mapJson);
                } else {
                    console.log("window.L", window.L);
                    console.log("地图元素：", document.getElementById(this.id));
                    window.setTimeout(this.loadMap, 10);
                }
            },
            loadMapByParam(paramConfig) {
                if (typeof (window.L) != "undefined" && typeof (window.L.Proj) != "undefined" && this.id != "" && typeof (document.getElementById(this.id)) != "undefined") {
                    console.log("地图id值", this.id);
                    var mapJson = paramConfig;
                    mapJson.id = this.id;
                    mapJson.useType = this.type;
                    this.initMap(mapJson);
                } else {
                    console.log("window.L", window.L);
                    console.log("地图元素：", document.getElementById(this.id));
                    window.setTimeout(this.loadMap, 10);
                }
            },
            //添加geo图层
            addGeoMap(geoParam, type) {
                this.geoMapTileLayer = null;
                if (type == "url") {
                    this.geoMapData = JSON.parse(this.readFile(geoParam));
                    this.geoMapTileLayer = L.geoJSON(this.geoMapData,
                        {
                            style: function (feature) {
                                return {color: '#c4deff', fillColor: '#00008B', weight: '2'};
                            }
                        }
                    );
                } else if (type == "data") {
                    this.geoMapTileLayer = L.geoJSON(geoParam,
                        {
                            style: function (feature) {
                                return {color: '#c4deff', fillColor: '#00008B', weight: '2'};
                            }
                        }
                    );
                }
                this.geoMapTileLayer.type = "geoTileLayer";
                this.geoMapTileLayer.addTo(this.map);
            },
            //文件数据读取
            readFile(filePath) {
                // 创建一个新的xhr对象
                let xhr = null;
                if (window.XMLHttpRequest) {
                    xhr = new XMLHttpRequest()
                } else {
                    // eslint-disable-next-line
                    xhr = new ActiveXObject('Microsoft.XMLHTTP')
                }
                const okStatus = document.location.protocol === 'file' ? 0 : 200;
                xhr.open('GET', filePath, false);
                xhr.overrideMimeType('text/html;charset=utf-8');
                xhr.send(null);
                return xhr.status === okStatus ? xhr.responseText : null;
            },

            /**
             * Leaflet地图加载
             */
            initMap(mapJson) {
                window.cbcFilter = "";
                if (typeof (mapJson.filter) != "undefined" && mapJson.filter != "") {
                    window.cbcFilter = mapJson.filter;
                }
                this.geoMapTileLayer = null;
                console.log("initMap mapJson", mapJson);
                if (mapJson.useType == "geoJsonMap") {
                    return;
                }
                var crsTemp = null;
                console.log("crs:", mapJson.crs);
                if (typeof (mapJson.crs) != "undefined") {
                    if (typeof (mapJson.crs.bounds) != "undefined") {
                        crsTemp = new L.Proj.CRS(mapJson.crs.epsg,
                            mapJson.crs.proj,
                            {
                                resolutions: mapJson.crs.resolutions,
                                origin: mapJson.crs.origin,
                                bounds: L.bounds(mapJson.crs.bounds[0], mapJson.crs.bounds[1])
                            });
                    } else {
                        crsTemp = new L.Proj.CRS(mapJson.crs.epsg,
                            mapJson.crs.proj,
                            {
                                resolutions: mapJson.crs.resolutions,
                                origin: mapJson.crs.origin,
                            });
                    }
                } else {
                    console.log("没有配置投影CRS");
                }


                if (this.map != null) {
                    try {
                        this.map.remove();
                    } catch (e) {
                        console.log(e);
                    }
                }
                var mapConfig = mapJson;
                var mapOption = {};
                mapOption.closePopupOnClick = mapConfig.closePopupOnClick;
                mapOption.maxZoom = mapConfig.maxZoom;
                mapOption.minZoom = mapConfig.minZoom;
                mapOption.zoomControl = mapConfig.zoomControl;
                mapOption.attributionControl = false;//右下角标题
                if (typeof (mapConfig.zoomSnap) != "undefined") {
                    mapOption.zoomSnap = mapConfig.zoomSnap;//缩放级别
                    mapOption.zoomDelta = mapConfig.zoomSnap;//缩放级别
                }
                if (crsTemp != null) {
                    mapOption.crs = crsTemp;
                }
                //地图初始化
                try {
                    this.map = L.map(mapConfig.id, mapOption);
                } catch (e) {
                    console.log("地图初始化错误信息", e);
                    window.setTimeout(this.loadMap, 100);
                }
                console.log("this.map:", this.map);
                if (typeof (this.map) == "undefined") {
                    return;
                }
                //地图坐标系
                this.map.coordinateType = mapConfig.coordinateType;
                //地图距离单位
                this.map.coordinateUnit = mapConfig.coordinateUnit;
                //动态图标移动时候居中
                this.map.movingMarker_flag = false;
                //为地图对象添加mapType赋值
                this.map.mapType = "leaflet";
                //为地图对象添加useType赋值
                this.map.useType = mapConfig.useType;
                this.useType = mapConfig.useType;
                var tileLayerOption = {};
                /**
                 * 百度坐标系需要加上tms参数,高德,PGIS则不需要
                 */
                //console.log("tms:",typeof (mapConfig.tms));
                if (typeof (mapConfig.tms) != "undefined") {
                    tileLayerOption.tms = mapConfig.tms;
                }
                //用于在地图上加载和显示切片图层
                var tileLayer = new L.TileLayer(mapConfig.mapUrl, tileLayerOption);
                tileLayer.type = "tileLayer";
                tileLayer.addTo(this.map);
                //初始定位
                if (typeof (this.initPosition.level) == "undefined") {
                    this.map.setView(mapConfig.centerPoint, mapConfig.level);
                } else {
                    this.map.setView(this.initPosition.centerPoint, this.initPosition.level);
                }
                if (typeof (mapConfig.titleUrl) != "undefined") {
                    //用于在地图上加载和显示切片图层
                    var otherLayer = new L.TileLayer(mapConfig.titleUrl, tileLayerOption);
                    otherLayer.type = "otherTileLayer";
                    otherLayer.addTo(this.map);
                }
                //地图加载完成
                if (typeof (this.loadEnd) != "undefined") {
                    if (mapJson.useType == "js") {
                        window[this.loadEnd]();
                    } else {
                        this.$parent[this.loadEnd]();
                    }
                }
            },
            /**
             * 添加地图图层
             */
            addTileLayer(url,option){
                var tileLayer=L.tileLayer(url, option).addTo(this.map);
                return tileLayer;
            },
            /**
             * 删除地图图层
             */
            removeTileLayer(tileLayer){
                console.log("tileLayer",tileLayer);
                this.map.removeLayer(tileLayer);
            },

            /**
             * 添加缩放控件
             */
            addZoomControl: function (position) {
                var zoomControl = L.control.zoom({position: position});
                zoomControl.addTo(this.map);
                zoomControl.name = "缩放控件";
                return zoomControl;
            },
            /**
             * 添加平移控件
             */
            addPanControl: function (position) {
                var panControl = L.control.pan({position: position});
                panControl.addTo(this.map);
                panControl.name = "平移控件";
                return panControl;
            },
            /**
             * 移除缩放控件
             */
            removeZoomControl: function (object) {
                object.remove();
            },
            /**
             * 移除平移控件
             */
            removePanControl: function (object) {
                object.remove();
            },
            /**
             * 添加比例尺控件
             */
            addScaleControl: function (position) {
                var scaleControl = L.control.scale({position: position,imperial:false});
                scaleControl.addTo(this.map);
                scaleControl.name = "比例尺控件";
                return scaleControl;
            },
            /**
             * 移除比例尺控件
             */
            removeScaleControl: function (object) {
                object.remove();
            },

            /**
             * 地图添加点
             */
            addMarker: function (markerJSON) {
                //console.log("leaflet markerJSON参数值:", markerJSON);
                var iconJson = {};
                iconJson.option = {};
                iconJson.option.iconUrl = markerJSON.iconUrl;
                iconJson.option.iconAnchor = markerJSON.iconAnchor;//坐标固定在图标的哪个点，[0,0]表示图标的左上角
                iconJson.option.iconSize = [markerJSON.width, markerJSON.height];

                var markerIcon = L.icon(iconJson.option);

                var markerJsonTemp = {};
                markerJsonTemp.option = {};
                markerJsonTemp.option.icon = markerIcon;
                if (typeof (markerJSON.option.draggable) != "undefined") {
                    markerJsonTemp.option.draggable = markerJSON.option.draggable;
                }
                var marker = new L.marker(markerJSON.xy, markerJsonTemp.option);
                marker.layerType = "marker";
                marker.addTo(this.map);
                return marker;
            },
            //设置marker层级
            setZIndexOffset: function (markerobj,zindex) {
                markerobj.setZIndexOffset(zindex);
            },
            /**
             * 画线d
             **/
            addPolyline: function (polylineJson) {
                var polyline = L.polyline(polylineJson.xys, polylineJson.option);
                polyline.layerType = "polyline";
                polyline.addTo(this.map);
                return polyline;
            },
            /**
             * 画面
             **/
            addPolygon: function (polygonJSON) {
                var polygon = L.polygon(polygonJSON.xys, polygonJSON.option);
                polygon.layerType = "polygon";
                polygon.zbc = polygonJSON.xys;
                var layerBound = this.getLayerBound(polygon);
                polygon.centerPoint = {};
                polygon.centerPoint.lat = (layerBound.south + layerBound.north) / 2;
                polygon.centerPoint.lng = (layerBound.east + layerBound.west) / 2;
                polygon.addTo(this.map);
                return polygon;
            },
            /**
             * 画矩形
             */
            addRect(rectJson) {
                var rect = L.rectangle(rectJson.xys, rectJson.option);
                rect.layerType = "rect";
                rect.zbc = rectJson.xys;
                var layerBound = this.getLayerBound(rect);
                rect.centerPoint = {};
                rect.centerPoint.lat = (layerBound.south + layerBound.north) / 2;
                rect.centerPoint.lng = (layerBound.east + layerBound.west) / 2;
                rect.addTo(this.map);
                return rect;
            },
            /**
             * 画圆
             */
            addCircle(circleJson) {
                if (this.map.coordinateType == "WGS84" && this.map.coordinateUnit == "DEGREE") {
                    //var radiusTemp=(1000*111.11)*circleJson.option.radius;
                    //circleJson.option.radius=radiusTemp;
                    circleJson.option.radius = this.meterToDegree(circleJson.option.radius);
                    var circle = L.circle(circleJson.xy, circleJson.option);
                    circle.layerType = "circle";
                    circle.radius = this.degreeToMeter(circleJson.option.radius);
                    circle.centerPoint = circle._latlng;
                    circle.addTo(this.map);
                } else {
                    var circle = L.circle(circleJson.xy, circleJson.option);
                    circle.layerType = "circle";
                    circle.centerPoint = circleJson.option.radius;
                    circle.centerPoint = circle._latlng;
                    circle.addTo(this.map);
                }
                return circle;
            },
            /**
             * 画div方法（json格式）
             * divJson.point坐标串数据
             * divJson.color线的颜色
             * divJson.content气泡框内容
             */
            addDiv(divJson) {
                divJson.option.iconSize = [];
                var divIcon = L.divIcon(divJson.option);
                var divLayer = L.marker(divJson.xy, {icon: divIcon});
                divLayer.type = "div";
                divLayer.addTo(this.map);
                return divLayer;
            },
            /**
             * 点，线，面绑定气泡框
             */
            bindPopup: function (object, option) {
                object.bindPopup(object.html, option);
            },

            /**
             * 地图添加气泡框
             */
            addPopup: function (option, point, html) {
                var popup = L.popup(option)
                    .setLatLng([point[0], point[1]])
                    .setContent(html)
                    .openOn(this.map);
                return popup;
            },

            /**
             *  中心点居中
             */
            panTo(point) {
                this.map.panTo(point);
            },
            /**
             *  级别缩放
             */
            zoomTo: function (level) {
                this.map.setZoom(level);
            },
            /**
             * 缩放并居中
             */
            setView: function (point, level) {
                this.map.setView(point, level);
            },

            /**
             * 删除图层
             */
            removeLayer: function (layer) {
                this.map.removeLayer(layer);
            },
            /**
             * 删除所有气泡框
             */
            closeAllPopup: function () {
                this.map.closePopup();
            },


            setIcon: function (layer, iconJson) {
                var _icon = this.createIcon(iconJson);
                layer.setIcon(_icon);
            },

            /**
             * 样式切换
             */
            setStyle: function (layer, option) {
                layer.setStyle(option);
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
                var bound = {};
                bound.east = layerObject.getBounds().getEast();
                bound.west = layerObject.getBounds().getWest();
                bound.south = layerObject.getBounds().getSouth();
                bound.north = layerObject.getBounds().getNorth();
                return bound;
            },
            /**
             * 根据图层边框居中
             */
            setCenterByBound: function (boundObject) {
                var corner1 = L.latLng(boundObject.south, boundObject.west);
                var corner2 = L.latLng(boundObject.north, boundObject.east);
                var bounds = L.latLngBounds(corner1, corner2);
                this.map.fitBounds(bounds);
            },

            /**
             * 绑定tip
             */
            bindTooltip: function (obj, content, option) {
                obj.bindTooltip(content, option);
            },

            //图层添加事件
            onLayerEvent: function (event,obj,callback,name) {
                obj.on(event, callback ,name);
                // obj.on(event, function (event) {
                //     callback(event);
                // },{name:name,callback:callback});
            },
            //图层移除事件
            offLayerEvent: function (event, obj, callback,name) {
                obj.off(event, callback ,name)
            },
            //地图绑定事件
            onMapEvent:function(event,callback,name){
                this.map.on(event, callback ,name);
            },
            //地图移除事件
            offMapEvent:function(event,callback,name){
                this.map.off(event, callback ,name);
            },


            /**
             * 聚合图方法
             * clusterPoints：聚合点
             * icon：聚合点图标
             */
            addCluster(clusterMarkers, option) {
                var ClusterLayer = L.markerClusterGroup(option);
                ClusterLayer.addLayers(clusterMarkers);
                this.map.addLayer(ClusterLayer);
                return ClusterLayer;
            },
            /**
             * 创建地图图标
             */
            createIcon(iconJson) {
                var tempIcon = L.icon(iconJson.option);
                return tempIcon;
            },
            /**
             * 创建marker，但是不进行上图
             * markerJson.xy xy坐标点
             * markerJson.option marker参数设置
             */
            createMarker(markerJson) {
                var marker = new L.marker(markerJson.xy, markerJson.option);
                return marker;
            },
            /**
             * 热力图方法addHeatMap
             * heatPoints：热力点数组
             * option:聚合半径等参数设置
             */
            addHeatMap(heatPoints, option) {
                var heatmap_layer = L.heatLayer(heatPoints, option);
                this.map.addLayer(heatmap_layer);
                return heatmap_layer;
            },

            /**
             * 蚂蚁线的方法
             * antPathJson.xys 坐标串
             * antPathJson.option 参数设置
             * @param antPathJson
             * @returns {*}
             */
            addAntPath(antPathJson) {
                var antPolyline = L.polyline.antPath(antPathJson.xys, antPathJson.option);
                antPolyline.addTo(this.map);
                return antPolyline;
            },
            /**
             * 波纹点的方法
             * pulseJson.xy  xy坐标
             * pulseJson.option  参数设置
             * @param pulseJson
             */
            addPulsingIcon(pulseJson) {
                var pulsingIcon = L.icon.pulse(pulseJson.option);
                var marker = L.marker(pulseJson.xy, {icon: pulsingIcon});
                marker.addTo(this.map);
                return marker;
            },
            /**
             * 添加散点图的方法
             * pulseJson.xy  xy坐标
             * pulseJson.option  参数设置
             * @param pulseJson
             */
            addScatter(scatterData, parts) {
                var markers = [];
                for (var i = 0; i < scatterData.length; i++) {
                    var pulseJson = {};
                    //xy坐标
                    pulseJson.xy = [scatterData[i].y, scatterData[i].x];
                    pulseJson.option = {};
                    var flag = 0;
                    for (var j = 0; j < parts.length; j++) {
                        //console.log(parts[j].betweenVal.length);
                        if (parts[j].betweenVal.length > 1) {
                            //console.log("scatterData:",window.scatterData[i].value);
                            //console.log(parts[j].betweenVal[0],parts[j].betweenVal[1]);
                            if (parts[j].betweenVal[0] <= scatterData[i].value && scatterData[i].value <= parts[j].betweenVal[1]) {
                                flag = 1;
                                //波纹大小
                                pulseJson.option.iconSize = [parts[j].size, parts[j].size];
                                //波纹颜色
                                pulseJson.option.color = parts[j].color;
                                //波纹圆点颜色
                                pulseJson.option.fillColor = parts[j].color;
                            }
                        }
                    }
                    if (flag == 0) {
                        for (var j = 0; j < parts.length; j++) {
                            if (parts[j].betweenVal.length == 1) {
                                //波纹大小
                                pulseJson.option.iconSize = [parts[j].size, parts[j].size];
                                //波纹颜色
                                pulseJson.option.color = parts[j].color;
                                //波纹圆点颜色
                                pulseJson.option.fillColor = parts[j].color;
                            }
                        }
                    }
                    //波纹速度（数值越大，速度越慢）
                    pulseJson.option.heartbeat = 2;
                    //是否显示波纹
                    pulseJson.option.animate = true;
                    var marker = this.addPulsingIcon(pulseJson);
                    marker.html = "<div>" + scatterData[i].name + ":" + scatterData[i].value + "</div>";
                    var option = {};
                    option.offset = [0, 0];//弹出偏移
                    this.bindPopup(marker, option);
                    marker.type = "marker";
                    markers.push(marker);
                }
                return markers;
            },
            /**
             * 添加蜂巢
             */
            addBeeHive(dataSet, options) {
                var mapvLayer = window.mapv.leafletMapLayer(dataSet, options).addTo(this.map);
                return mapvLayer;
            },

            /**
             * 虚线
             * dashJson.xys 坐标串
             * dashJson.option 参数设置
             * @param dashJson
             */
            addDashPolyline(dashJson) {
                var pathPattern = L.polylineDecorator(dashJson.xys,
                    {
                        patterns: [
                            {
                                offset: 10,
                                repeat: 20,
                                symbol: L.Symbol.dash({pixelSize: 10, pathOptions: dashJson.option})
                            },
                            //{offset: 0, repeat: 20, symbol: L.Symbol.dash({pixelSize: 1})}
                        ]
                    }
                ).addTo(this.map);
                pathPattern._latlngs=[];
                for(var i=0;i<dashJson.xys.length;i++){
                    var obj={};
                    obj.lat=dashJson.xys[i][0];
                    obj.lng=dashJson.xys[i][1];
                    pathPattern._latlngs.push(obj);
                }
                return pathPattern;
            },

            /**
             * 箭头线
             */
            addDirectionPolyline(polylineObj, option) {
                L.polylineDecorator(polylineObj, {
                    patterns: [
                        {
                            offset: 20,
                            repeat: 50,
                            symbol: L.Symbol.arrowHead({
                                pixelSize: option.arrowSize,
                                pathOptions: {fillOpacity: 1, weight: 0}
                            })
                        }
                    ]
                }).addTo(this.map);
            },
            /**
             * 图标线
             */
            addIconPolyline(iconPolylineObj) {
                L.polylineDecorator(iconPolylineObj, {
                    patterns: [
                        {
                            offset: 1, repeat: 100, symbol: L.Symbol.marker({
                                rotate: true, markerOptions: {
                                    icon: iconPolylineObj.icon
                                }
                            })
                        }
                    ]
                }).addTo(this.map);
            },
            /**
             * 平行线
             */
            addParallelPolyline(polylineJSON) {
                console.log("线配置：",polylineJSON);
                var polylineTemp = this.addPolyline(polylineJSON);
                var _rings=polylineTemp._rings;
                var newXYarr=[];
                for(var i=0;i<_rings[0].length;i++){
                    var point=this.map.layerPointToLatLng(_rings[0][i]);
                    newXYarr.push(point);
                }
                console.log("偏移坐标：",newXYarr);
                var option={};
                option=polylineJSON.option;
                delete option.offset;
                var parallelPolyline = L.polyline(newXYarr, option).addTo(this.map);
                this.removeLayer(polylineTemp);
                return parallelPolyline;
            },
            //线段尾巴加图标
            addDirectionIcon(polylineObj) {
                var markers=[];
                console.log("线对象", polylineObj);
                var latlngs = polylineObj._latlngs;
                for (var i = 0; i < latlngs.length; i++) {
                    if (i == 0) {

                    } else {
                        var startPoint = latlngs[i - 1];
                        var endPoint = latlngs[i];
                        var angle = Math.atan2((endPoint.lng - startPoint.lng), (endPoint.lat - startPoint.lat)); //弧度  0.6435011087932844
                        //var angle = Math.atan2((endPoint.lat - startPoint.lat), (endPoint.lng - startPoint.lng)); //弧度  0.6435011087932844
                        var theta = angle * (180 / Math.PI); //角度  36.86989764584402
                        //console.log("rotateRatio", angle, theta);
                        //添加箭头
                        var divJson2 = {};
                        divJson2.xy = [endPoint.lat, endPoint.lng];
                        divJson2.option = {};
                        divJson2.option.iconAnchor = polylineObj.icon.options.iconAnchor;
                        var html = "";
                        html += '<div>';
                        html += '<img src="'+polylineObj.icon.options.iconUrl+'" width="'+polylineObj.icon.options.iconSize[0]+'px" height="'+polylineObj.icon.options.iconSize[1]+'px" style="transform: rotateZ('+theta+'deg)">';
                        html += '</div>';
                        divJson2.option.html = html;
                        var directionDiv=this.addDiv(divJson2);
                        markers.push(directionDiv);
                    }
                }
                return markers;
            },

            /**
             * 图标移动
             * movingMarkerJson.xys : 坐标串
             * movingMarkerJson.xys[i].popup : 为每个坐标创建气泡框
             * movingMarkerJson.icon : 创建移动图标
             * movingMarkerJson.speed : 移动速度（米/秒）
             */
            addMovingMarker(movingMarkerJson) {
                let the = this;

                var times = [];
                var point_popups = [];

                for (var i = 0; i < movingMarkerJson.xys.length; i++) {
                    movingMarkerJson.xys[i].popup.index = i;
                    movingMarkerJson.xys[i].popup.xy = movingMarkerJson.xys[i];
                    point_popups.push(movingMarkerJson.xys[i].popup);
                    //查询两点之间的距离
                    if (i >= 1) {
                        var meter = this.distace(movingMarkerJson.xys[i], movingMarkerJson.xys[i - 1]);
                        var timeTemp = parseInt((meter / movingMarkerJson.speed) * 1000);
                        times.push(timeTemp);
                    }

                }

                the.map.point_popups = point_popups;
                the.map.movingMarkerShowPopup = movingMarkerJson.movingMarkerShowPopup;
                the.map.showPolyline_flag = movingMarkerJson.showPolyline_flag;
                the.map.showTail_flag = movingMarkerJson.showTail_flag;
                the.map.showTailByPoint_flag = movingMarkerJson.showTailByPoint_flag;
                the.map.tailPoint = movingMarkerJson.tailPoint;
                the.map.movingMarkerEndRemoveFlag = movingMarkerJson.movingMarkerEndRemoveFlag;

                var moving_mark = L.Marker.movingMarker(movingMarkerJson.xys, times, {
                    icon: movingMarkerJson.icon,
                    loop: false
                }).addTo(the.map);

                the.map.movingMarker_flag = movingMarkerJson.movingMarker;
                moving_mark.start();
                moving_mark.once('click', function () {
                    moving_mark.start();
                    moving_mark.on('click', function () {
                        if (moving_mark.isRunning()) {
                            moving_mark.pause();
                        } else {
                            moving_mark.start();
                        }
                    });
                    setTimeout(function () {

                    }, 100);
                });
                moving_mark.on('end', function () {
                    //console.log("最后一个点");
                    //cbc-edit-start
                    the.map.closePopup();
                    //console.log(the.map.point_popups);
                    if (the.map.movingMarkerShowPopup) {
                        the.map.point_popups[the.map.point_popups.length - 1].openOn(the.map);
                    }
                    //cbc-edit-end
                });

                return moving_mark;
            },

            /**
             * 动态画线
             * @param points ： 坐标串点
             * @param _icon ：移动图标
             * @returns {*}
             */
            addMovingPolyline(points, _icon) {
                var featureGroup = [];
                for (var i = 0; i < points.length; i++) {
                    if (i == points.length - 1) {
                        featureGroup.push(L.marker(points[i], {icon: _icon}));
                    } else {
                        featureGroup.push(L.marker(points[i], {icon: _icon}));
                        featureGroup.push(L.polyline([points[i], points[i + 1]]));
                    }
                }

                var featureGroup = L.featureGroup(featureGroup);
                this.map.addLayer(featureGroup);
                return featureGroup;
            },

            addPolygonByGeojson(areaBoundaryPoints, option) {
                var geojson = JSON.parse(areaBoundaryPoints);
                var layer = L.geoJson(geojson, option).addTo(this.map);
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
             * 创建气泡框
             */
            createPopup(popupJson) {
                var popup = L.popup(popupJson.option)
                    .setLatLng(popupJson.xy)
                    .setContent(popupJson.content);
                return popup;
            },

            /**
             * 采集点
             * markerIcon:采集点图标
             * dragFlag:是否可拖动
             */
            drawMarker: function (markerJSON, dragFlag, callback) {
                let the = this;
                this.map.on('click', function (e) {
                    var iconJson = {};
                    iconJson.option = {};
                    iconJson.option.iconUrl = markerJSON.iconUrl;
                    iconJson.option.iconAnchor = markerJSON.iconAnchor;//坐标固定在图标的哪个点，[0,0]表示图标的左上角
                    iconJson.option.iconSize = [markerJSON.width, markerJSON.height];
                    var gatherIcon = L.icon(iconJson.option);
                    //第一种方法（draggable: true）
                    let gather_marker_layer;
                    //第二种方法（gather_marker_layer.options.draggable=true;）
                    if (dragFlag == true) {
                        gather_marker_layer = new L.marker([e.latlng.lat, e.latlng.lng], {
                            icon: gatherIcon,
                            draggable: true
                        });
                        gather_marker_layer.options.draggable = true;
                        gather_marker_layer.on('drag', function (e) {
                            gather_marker_layer.xy = gather_marker_layer._latlng;
                            callback(gather_marker_layer);
                        });
                    } else {
                        gather_marker_layer = new L.marker([e.latlng.lat, e.latlng.lng], {
                            icon: gatherIcon,
                            draggable: false
                        });
                    }
                    gather_marker_layer.addTo(the.map);
                    the.map.off("click");//移除所有监听对象
                    gather_marker_layer.xy = gather_marker_layer._latlng;
                    callback(gather_marker_layer);
                });
            },
            /**
             * 采集线
             */
            drawPolyline: function (callback) {
                this.clearHooks();
                let the = this;
                this.draw_polyline = new L.Draw.Polyline(this.map);
                this.draw_polyline.addHooks();
                this.map.on('draw:created', function (e) {
                    var gather_polyline_layer = e.layer;
                    the.map.addLayer(gather_polyline_layer);
                    the.draw_polyline.removeHooks();
                    the.map.off("draw:created");
                    var zbc = gather_polyline_layer.getLatLngs();
                    gather_polyline_layer.zbc = zbc;
                    callback(gather_polyline_layer);
                });
            },
            /**
             * 删除采集时最后一个点
             */
            drawDeleteLastVertex() {
                if (this.draw_polyline != null) {
                    this.draw_polyline.deleteLastVertex();
                }
                if (this.draw_polygon != null) {
                    this.draw_polygon.deleteLastVertex();
                }
            },
            /**
             * 采集面
             */
            drawPolygon: function (callback) {
                this.clearHooks();
                var the = this;
                this.draw_polygon = new L.Draw.Polygon(this.map);
                this.draw_polygon.addHooks();
                this.map.on('draw:created', function (e) {
                    var gather_polygon_layer = e.layer;
                    the.map.addLayer(gather_polygon_layer);
                    the.draw_polygon.removeHooks();
                    the.map.off("draw:created");
                    var zbc = [];
                    for(var i=0;i<gather_polygon_layer.getLatLngs()[0].length;i++){
                        zbc.push([gather_polygon_layer.getLatLngs()[0][i].lat,gather_polygon_layer.getLatLngs()[0][i].lng]);
                    }
                    gather_polygon_layer.zbc = zbc;
                    callback(gather_polygon_layer);
                });
            },
            /**
             * 采集矩形
             */
            drawRect: function (callback) {
                this.clearHooks();
                let the = this;
                this.draw_rect = new L.Draw.Rectangle(this.map);
                this.draw_rect.addHooks();
                this.map.on('draw:created', function (e) {
                    var gather_rect_layer = e.layer;
                    the.map.addLayer(gather_rect_layer);
                    the.draw_rect.removeHooks();
                    the.map.off("draw:created");

                    var zbc = [];
                    for(var i=0;i<gather_rect_layer.getLatLngs()[0].length;i++){
                        zbc.push([gather_rect_layer.getLatLngs()[0][i].lat,gather_rect_layer.getLatLngs()[0][i].lng]);
                    }
                    gather_rect_layer.zbc = zbc;
                    callback(gather_rect_layer);
                });
            },
            /**
             * 采集圆
             */
            drawCircle: function (callback) {
                this.clearHooks();
                let the = this;
                console.log("地图单位：", the.map.coordinateUnit);
                this.map.on('preclick', function (e) {
                    var gather_circle_layer = L.circle(e.latlng, {radius: 0});
                    gather_circle_layer.addTo(the.map);
                    the.map.off("preclick");
                    //the.map.dragging.enable();
                    the.clearHooks();
                    gather_circle_layer.centerPoint = gather_circle_layer._latlng;
                    if (the.map.coordinateUnit == "METER") {
                        gather_circle_layer.radius = gather_circle_layer._mRadius;
                    } else {
                        gather_circle_layer.radius = gather_circle_layer._mRadius * (1000 * 101.11);
                    }
                    callback(gather_circle_layer);
                });

                the.draw_circle = new L.Draw.Circle(the.map);
                the.draw_circle.addHooks();
                the.map.on('draw:created', function (e) {
                    var gather_circle_layer = e.layer;
                    the.map.addLayer(gather_circle_layer);
                    the.draw_circle.removeHooks();
                    the.map.off("draw:created");
                    the.map.off("preclick");
                    gather_circle_layer.centerPoint = gather_circle_layer._latlng;
                    if (the.map.coordinateUnit == "METER") {
                        gather_circle_layer.radius = gather_circle_layer._mRadius;
                    } else {
                        gather_circle_layer.radius = gather_circle_layer._mRadius * (1000 * 101.11);
                    }
                    callback(gather_circle_layer);
                });

            },

            /**
             * 点（已在地图上）进入编辑状态
             * markerObj:要编辑的点对象
             */
            startEditMarker(markerObj) {
                markerObj.xy = markerObj._latlng;
                markerObj.dragging.enable();
                return markerObj;
            },
            /**
             * 点（已在地图上）结束编辑状态
             * markerObj:要结束编辑的点对象
             */
            endEditMarker(markerObj) {
                markerObj.dragging.disable();
                markerObj.off('drag');
                markerObj.xy = markerObj._latlng;
                return markerObj;
            },

            /**
             * 线（已在地图上）进入编辑状态
             * polylineObj:要编辑的线对象
             */
            startEditPolyline(polylineObj) {
                var zbc = polylineObj.getLatLngs();
                polylineObj.zbc = zbc;
                polylineObj.editing.enable();
                return polylineObj;
            },
            /**
             * 线（已在地图上）结束编辑状态
             * polylineObj:要结束编辑的线对象
             */
            endEditPolyline(polylineObj) {
                polylineObj.editing.disable();
                this.map.off('mouseup');
                var zbc = polylineObj.getLatLngs();
                polylineObj.zbc = zbc;
                return polylineObj;
            },

            /**
             * 启动多边形编辑状态
             * polygonObj:要编辑的多边形对象
             */
            startEditPolygon(polygonObj) {
                var zbc = polygonObj.getLatLngs()[0];
                polygonObj.zbc = zbc;
                polygonObj.editing.enable();
                return polygonObj;
            },
            /**
             * 结束多边形编辑状态
             * polygonObj:要编辑的多边形对象
             */
            endEditPolygon(polygonObj) {
                polygonObj.editing.disable();
                this.map.off('mouseup');
                var zbc = polygonObj.getLatLngs()[0];
                polygonObj.zbc = zbc;
                return polygonObj;
            },
            /**
             * 圆进入编辑状态
             * circleObj:要编辑的圆对象
             */
            startEditCircle(circleObj) {
                circleObj.centerPoint = circleObj._latlng;
                circleObj.radius = circleObj._mRadius * (1000 * 101.11);
                circleObj.editing.enable();
                return circleObj;
            },
            /**
             * 圆进入编辑状态
             * circleObj:要结束编辑的圆对象
             */
            endEditCircle(circleObj) {
                circleObj.editing.disable();
                this.map.off('mouseup');
                circleObj.centerPoint = circleObj._latlng;
                circleObj.radius = circleObj._mRadius * (1000 * 101.11);
                return circleObj;
            },
            /**
             * 矩形进入编辑状态
             * rectObj:要编辑的矩形对象
             */
            startEditRect(rectObj) {
                rectObj.editing.enable();
                var zbc = rectObj.getLatLngs()[0];
                rectObj.zbc = zbc;
            },
            /**
             * 矩形结束编辑状态
             * rectObj:要结束编辑的矩形对象
             */
            endEditRect(rectObj) {
                rectObj.editing.disable();
                this.map.off('mouseup');
                var zbc = rectObj.getLatLngs()[0];
                rectObj.zbc = zbc;
                return rectObj;
            },

            clearHooks: function () {
                this.map.off("draw:created");
                //圆编辑
                if (this.draw_circle != null) {
                    try {
                        this.draw_circle.removeHooks();
                        this.draw_circle = null;
                    } catch (e) {

                    }
                }
                //矩形编辑
                if (this.draw_rect != null) {
                    try {
                        this.draw_rect.removeHooks();
                        this.draw_rect = null;
                    } catch (e) {

                    }
                }
                //多边形编辑
                if (this.draw_polygon != null) {
                    try {
                        this.draw_polygon.removeHooks();
                        this.draw_polygon = null;
                    } catch (e) {

                    }
                }
                //线编辑
                if (this.draw_polyline != null) {
                    try {
                        this.draw_polyline.removeHooks();
                        this.draw_polyline = null;
                    } catch (e) {

                    }
                }
            },


            //流出迁徙图
            outMigration: function (outPolylineData) {
                this.outPolylineData = outPolylineData;
                var allCount = 0;
                //终点波纹点
                for (var i = 0; i < outPolylineData.length; i++) {
                    allCount = allCount + (Number(outPolylineData[i].count));
                    var endDivJson = {};
                    endDivJson.xy = outPolylineData[i].to;
                    endDivJson.option = {};
                    endDivJson.option.iconAnchor = [23, 55];
                    var html = '';
                    html += '<div align="center" style="text-align:center;line-height:40px;background-color:white;border-radius:40px;width:40px;height:40px;border-style:solid;border-color:' + endDivJson.xy.color + '">';
                    html += '<span style="font-weight: bold;font-size: 20px">' + (Number(outPolylineData[i].count)) + '</span>';
                    html += '<div class="peoplePhoto2-box" style="border-top: 10px solid ' + endDivJson.xy.color + ';"></div>';
                    html += '</div>';
                    endDivJson.option.html = html;
                    var endDiv = this.addDiv(endDivJson);
                    endDiv.bindTooltip(outPolylineData[i].from.label + "-" + outPolylineData[i].to.label + "进出总数", {
                        direction: 'top',
                        offset: [0, 0],
                        permanent: false,
                        opacity: 1,
                        sticky: true
                    });
                    this.arrInfo.push(endDiv);
                }
                //起点波纹点
                var fromPoint = null;
                if (allCount > 0) {
                    fromPoint = outPolylineData[0].from;
                    //起点波纹点
                    var startDivJson = {};
                    startDivJson.xy = fromPoint;
                    startDivJson.option = {};
                    startDivJson.option.iconAnchor = [23, 55];

                    var html = '';
                    html += '<div align="center" style="text-align:center;line-height:40px;background-color:white;border-radius:40px;width:40px;height:40px;border-style:solid;border-color:' + startDivJson.xy.color + '">';
                    html += '<span style="font-weight: bold;font-size: 20px">' + allCount + '</span>';
                    html += '<div class="peoplePhoto2-box" style="border-top: 10px solid ' + startDivJson.xy.color + ';"></div>';
                    html += '</div>';
                    startDivJson.option.html = html;
                    var startDiv = this.addDiv(startDivJson);
                    startDiv.bindTooltip(fromPoint.label + "进出各地总数", {
                        direction: 'top',
                        offset: [0, 0],
                        permanent: false,
                        opacity: 1,
                        sticky: true
                    });
                    this.arrInfo.push(startDiv);
                }
                //添加曲线
                this.canvasLayer = this.addCanvas(this, "outCurve");
                //添加动态点
                this.cavasMoveMarkerInterval = setInterval(this.outMoveMarker, 60);

                //地图开始缩放--删除点
                let that = this;
                this.map.on("zoomstart", function () {
                    if (that.cavasMoveMarkerInterval != null) {
                        window.clearInterval(that.cavasMoveMarkerInterval);
                        that.cavasMoveMarkerInterval = null;
                    }
                    if (that.canvasMoveMarker != null) {
                        that.removeLayer(that.canvasMoveMarker);
                        that.canvasMoveMarker = null;
                    }
                });
                //地图结束缩放--开启marker移动
                this.map.on("zoomend", function () {
                    if (that.geoMapTileLayer != null && that.geoMapData != "") {
                        //console.log("this.geoMapTileLayer",that.geoMapTileLayer);
                        that.map.removeLayer(that.geoMapTileLayer);
                        that.addGeoMap(that.geoMapData, "data");
                    }
                    if (that.cavasMoveMarkerInterval == null) {
                        that.cavasMoveMarkerInterval = setInterval(that.outMoveMarker, 60);
                    }
                });
                this.canvasRefresh();
            },
            //添加动态点
            outMoveMarker() {
                if (this.canvasMoveMarker != null) {
                    this.removeLayer(this.canvasMoveMarker);
                    this.canvasMoveMarker = null;
                }
                this.canvasMoveMarker = this.addCanvas(this, "onOutMoveMarker");
            },
            onOutMoveMarker(info) {
                for (var i = 0; i < this.outPolylineData.length; i++) {
                    var ctx = info.canvas.getContext('2d');
                    var fromPoint = this.outPolylineData[i].from;
                    var toPoint = this.outPolylineData[i].to;
                    this.outPolylineData[i].i = i;
                    var markerColor = this.outPolylineData[i].color;
                    this.addCanvasCurveMovePoint(ctx, fromPoint, toPoint, info, this.outPolylineData[i], markerColor);
                }
            },
            /**
             * 添加曲线
             */
            outCurve: function (info) {
                var ctx = info.canvas.getContext('2d');
                ctx.clearRect(0, 0, info.canvas.width, info.canvas.height);
                //起点到终点的曲线
                for (var i = 0; i < this.outPolylineData.length; i++) {
                    var fromPoint = this.outPolylineData[i].from;
                    var toPoint = this.outPolylineData[i].to;
                    var centerLabel = this.outPolylineData[i].count;
                    var curveColor1 = this.outPolylineData[i].color;
                    //添加曲线
                    this.addCanvasCurve(ctx, fromPoint, toPoint, curveColor1);
                    //添加曲线中心
                    //this.addCurveCenter(ctx, fromPoint, toPoint, centerLabel, curveColor1);
                    //添加曲线箭头
                    this.addCurveArrow(ctx, fromPoint, toPoint, curveColor1);
                }
            },

            //流入迁徙图
            inMigration: function (inPolylineData) {
                this.inPolylineData = inPolylineData;
                var allCount = 0;
                //起点波纹点
                for (var i = 0; i < inPolylineData.length; i++) {
                    allCount = allCount + (Number(inPolylineData[i].count));
                    var endDivJson = {};
                    endDivJson.xy = inPolylineData[i].from;
                    endDivJson.option = {};
                    endDivJson.option.iconAnchor = [23, 55];
                    //console.log("444444444444444444444455",endDivJson.xy.color);
                    var html = '';
                    html += '<div align="center" style="text-align:center;line-height:40px;background-color:white;border-radius:40px;width:40px;height:40px;border-style:solid;border-color:' + endDivJson.xy.color + '">';
                    html += '<span style="font-weight: bold;font-size: 20px">' + (Number(inPolylineData[i].count)) + '</span>';
                    html += '<div class="peoplePhoto2-box"  style="border-top: 10px solid ' + endDivJson.xy.color + ';"></div>';
                    html += '</div>';
                    endDivJson.option.html = html;
                    var endDiv = this.addDiv(endDivJson);
                    endDiv.bindTooltip(inPolylineData[i].from.label + "-" + inPolylineData[i].to.label + "进出总数", {
                        direction: 'top',
                        offset: [0, 0],
                        permanent: false,
                        opacity: 1,
                        sticky: true
                    });
                    this.arrInfo.push(endDiv);
                }

                //终点波纹点
                var toPoint = null;
                if (allCount > 0) {
                    toPoint = inPolylineData[0].to;
                    //起点波纹点
                    var startDivJson = {};
                    startDivJson.xy = toPoint;
                    startDivJson.option = {};
                    startDivJson.option.iconAnchor = [23, 55];
                    var html = '';
                    html += '<div align="center" style="text-align:center;line-height:40px;background-color:white;border-radius:40px;width:40px;height:40px;border-style:solid;border-color:' + startDivJson.xy.color + '">';
                    html += '<span style="font-weight: bold;font-size: 20px">' + allCount + '</span>';
                    html += '<div class="peoplePhoto2-box" style="border-top: 10px solid ' + startDivJson.xy.color + ';"></div>';
                    html += '</div>';
                    startDivJson.option.html = html;
                    var startDiv = this.addDiv(startDivJson);
                    startDiv.bindTooltip(toPoint.label + "进出各地总数", {
                        direction: 'top',
                        offset: [0, 0],
                        permanent: false,
                        opacity: 1,
                        sticky: true
                    });
                    this.arrInfo.push(startDiv);
                }
                //添加曲线
                this.canvasLayer = this.addCanvas(this, "inCurve");
                //添加动态点
                this.cavasMoveMarkerInterval = setInterval(this.inMoveMarker, 60);

                //地图开始缩放--删除点
                let that = this;
                this.map.on("zoomstart", function () {
                    if (that.cavasMoveMarkerInterval != null) {
                        window.clearInterval(that.cavasMoveMarkerInterval);
                        that.cavasMoveMarkerInterval = null;
                    }
                    if (that.canvasMoveMarker != null) {
                        that.removeLayer(that.canvasMoveMarker);
                        that.canvasMoveMarker = null;
                    }
                });
                //地图结束缩放--开启marker移动
                this.map.on("zoomend", function () {
                    if (that.geoMapTileLayer != null && that.geoMapData != "") {
                        //console.log("this.geoMapTileLayer",that.geoMapTileLayer);
                        that.map.removeLayer(that.geoMapTileLayer);
                        that.addGeoMap(that.geoMapData, "data");
                    }
                    if (that.cavasMoveMarkerInterval == null) {
                        that.cavasMoveMarkerInterval = setInterval(that.inMoveMarker, 60);
                    }
                });
                this.canvasRefresh();
            },
            //添加动态点
            inMoveMarker() {
                if (this.canvasMoveMarker != null) {
                    this.removeLayer(this.canvasMoveMarker);
                    this.canvasMoveMarker = null;
                }
                this.canvasMoveMarker = this.addCanvas(this, "onInMoveMarker");
            },
            onInMoveMarker(info) {
                for (var i = 0; i < this.inPolylineData.length; i++) {
                    var ctx = info.canvas.getContext('2d');
                    var fromPoint = this.inPolylineData[i].from;
                    var toPoint = this.inPolylineData[i].to;
                    this.inPolylineData[i].i = i;
                    var markerColor = this.inPolylineData[i].color;
                    this.addCanvasCurveMovePoint(ctx, fromPoint, toPoint, info, this.inPolylineData[i], markerColor);
                }
            },
            /**
             * 添加曲线
             */
            inCurve: function (info) {
                var ctx = info.canvas.getContext('2d');
                ctx.clearRect(0, 0, info.canvas.width, info.canvas.height);
                //起点到终点的曲线
                for (var i = 0; i < this.inPolylineData.length; i++) {
                    var fromPoint = this.inPolylineData[i].from;
                    var toPoint = this.inPolylineData[i].to;
                    //var centerLabel=this.inPolylineData[i].centerLabel;
                    var curveColor = this.inPolylineData[i].color;
                    //添加曲线
                    this.addCanvasCurve(ctx, fromPoint, toPoint, curveColor);
                    //添加曲线中心
                    //this.$refs.map.addCurveCenter(ctx,fromPoint,toPoint,centerLabel);
                    //添加曲线箭头
                    this.addCurveArrow(ctx, fromPoint, toPoint, curveColor);
                }
            },


            //双向迁徙图
            inOutMigration: function (outPolylineData, inPolylineData) {

                this.inPolylineData = inPolylineData;
                this.outPolylineData = outPolylineData;
                var allCount = 0;
                //终点波纹点
                for (var i = 0; i < outPolylineData.length; i++) {
                    allCount = allCount + (Number(inPolylineData[i].count) + Number(outPolylineData[i].count));
                    var endDivJson = {};
                    endDivJson.xy = outPolylineData[i].to;
                    endDivJson.option = {};
                    endDivJson.option.iconAnchor = [23, 55];
                    var html = '';
                    html += '<div align="center" style="text-align:center;line-height:40px;background-color:white;border-radius:40px;width:40px;height:40px;border-style:solid;border-color:' + endDivJson.xy.color + '">';
                    html += '<span style="font-weight: bold;font-size: 20px">' + (Number(inPolylineData[i].count) + Number(outPolylineData[i].count)) + '</span>';
                    html += '<div class="peoplePhoto2-box" style="border-top: 10px solid ' + endDivJson.xy.color + ';"></div>';
                    html += '</div>';
                    endDivJson.option.html = html;
                    var endDiv = this.addDiv(endDivJson);
                    endDiv.bindTooltip(outPolylineData[i].from.label + "-" + outPolylineData[i].to.label + "进出总数", {
                        direction: 'top',
                        offset: [0, 0],
                        permanent: false,
                        opacity: 1,
                        sticky: true
                    });
                    this.arrInfo.push(endDiv);
                }
                //起点波纹点
                var fromPoint = null;
                if (allCount > 0) {
                    fromPoint = outPolylineData[0].from;
                    //起点波纹点
                    var startDivJson = {};
                    startDivJson.xy = fromPoint;
                    startDivJson.option = {};
                    startDivJson.option.iconAnchor = [23, 55];

                    var html = '';
                    html += '<div align="center" style="text-align:center;line-height:40px;background-color:white;border-radius:40px;width:40px;height:40px;border-style:solid;border-color:' + startDivJson.xy.color + '">';
                    html += '<span style="font-weight: bold;font-size: 20px">' + allCount + '</span>';
                    html += '<div class="peoplePhoto2-box" style="border-top: 10px solid ' + startDivJson.xy.color + ';"></div>';
                    html += '</div>';
                    startDivJson.option.html = html;
                    var startDiv = this.addDiv(startDivJson);
                    startDiv.bindTooltip(fromPoint.label + "进出各地总数", {
                        direction: 'top',
                        offset: [0, 0],
                        permanent: false,
                        opacity: 1,
                        sticky: true
                    });
                    this.arrInfo.push(startDiv);
                }

                //添加曲线
                this.canvasLayer = this.addCanvas(this, "inOutCurve");

                //添加动态点
                this.cavasMoveMarkerInterval = setInterval(this.inOutMoveMarker, 60);

                //地图开始缩放--删除点
                let that = this;
                this.map.on("zoomstart", function () {
                    if (that.cavasMoveMarkerInterval != null) {
                        window.clearInterval(that.cavasMoveMarkerInterval);
                        that.cavasMoveMarkerInterval = null;
                    }
                    if (that.canvasMoveMarker != null) {
                        that.removeLayer(that.canvasMoveMarker);
                        that.canvasMoveMarker = null;
                    }
                });
                //地图结束缩放--开启marker移动
                this.map.on("zoomend", function () {
                    if (that.geoMapTileLayer != null && that.geoMapData != "") {
                        //console.log("this.geoMapTileLayer",that.geoMapTileLayer);
                        that.map.removeLayer(that.geoMapTileLayer);
                        that.addGeoMap(that.geoMapData, "data");
                    }
                    if (that.cavasMoveMarkerInterval == null) {
                        that.cavasMoveMarkerInterval = setInterval(that.inOutMoveMarker, 60);
                    }
                });
                this.canvasRefresh();
            },
            //添加动态点
            inOutMoveMarker() {
                if (this.canvasMoveMarker != null) {
                    this.removeLayer(this.canvasMoveMarker);
                    this.canvasMoveMarker = null;
                }
                this.canvasMoveMarker = this.addCanvas(this, "onInOutMoveMarker");
            },
            onInOutMoveMarker(info) {
                for (var i = 0; i < this.outPolylineData.length; i++) {
                    var ctx = info.canvas.getContext('2d');
                    var fromPoint = this.outPolylineData[i].from;
                    var toPoint = this.outPolylineData[i].to;
                    this.outPolylineData[i].i = i;
                    var markerColor = this.outPolylineData[i].color;
                    this.addCanvasCurveMovePoint(ctx, fromPoint, toPoint, info, this.outPolylineData[i], markerColor);
                }
                for (var i = 0; i < this.inPolylineData.length; i++) {
                    var ctx = info.canvas.getContext('2d');
                    var fromPoint = this.inPolylineData[i].from;
                    var toPoint = this.inPolylineData[i].to;
                    this.inPolylineData[i].i = i;
                    var markerColor = this.inPolylineData[i].color;
                    this.addCanvasCurveMovePoint(ctx, fromPoint, toPoint, info, this.inPolylineData[i], markerColor);
                }
            },
            /**
             * 添加曲线
             */
            inOutCurve: function (info) {
                var ctx = info.canvas.getContext('2d');
                ctx.clearRect(0, 0, info.canvas.width, info.canvas.height);
                //起点到终点的曲线
                for (var i = 0; i < this.outPolylineData.length; i++) {
                    var fromPoint = this.outPolylineData[i].from;
                    var toPoint = this.outPolylineData[i].to;
                    var centerLabel = this.outPolylineData[i].count;
                    var curveColor1 = this.outPolylineData[i].color;
                    //添加曲线
                    this.addCanvasCurve(ctx, fromPoint, toPoint, curveColor1);
                    //添加曲线中心
                    this.addCurveCenter(ctx, fromPoint, toPoint, centerLabel, curveColor1);
                    //添加曲线箭头
                    this.addCurveArrow(ctx, fromPoint, toPoint, curveColor1);
                }
                //起点到终点的曲线
                for (var i = 0; i < this.inPolylineData.length; i++) {
                    var fromPoint = this.inPolylineData[i].from;
                    var toPoint = this.inPolylineData[i].to;
                    var centerLabel = this.inPolylineData[i].count;
                    var curveColor = this.inPolylineData[i].color;
                    //添加曲线
                    this.addCanvasCurve(ctx, fromPoint, toPoint, curveColor);
                    //添加曲线中心
                    this.addCurveCenter(ctx, fromPoint, toPoint, centerLabel, curveColor);
                    //添加曲线箭头
                    //this.addCurveArrow(ctx,fromPoint,toPoint,curveColor);
                }

            },


            /**
             * 添加canvas
             */
            addCanvas(obj, fun) {
                var canvasLayer = L.canvasLayer()
                    .delegate(obj, fun)
                    .addTo(this.map);
                return canvasLayer;
            },
            /**
             * 地图上的canvas刷新
             */
            canvasRefresh() {
                var centerPoint = this.map.getCenter();
                var lat = centerPoint.lat;
                var lng = centerPoint.lng + 0.00000001;
                this.map.setView([lat, lng]);
            },
            /**
             *  添加canvas曲线
             * @param ctx
             * @param fromPoint
             * @param toPoint
             */
            addCanvasCurve(ctx, fromPoint, toPoint, curveColor) {
                //console.log("addCanvasCurve");
                var returnObj = this.calcalateXY(fromPoint, toPoint);
                ctx.beginPath();
                ctx.lineWidth = 2;
                //var grd = ctx.createLinearGradient(returnObj.startX,returnObj.startY,returnObj.endX,returnObj.endY);
                //grd.addColorStop(0, "#5BC0DE");
                //grd.addColorStop(0.5, "#ffff00");
                //grd.addColorStop(1, "#5BC0DE");
                ctx.strokeStyle = curveColor;
                ctx.arc(returnObj.centerX, returnObj.centerY, returnObj.radius, returnObj.startAngle, returnObj.endAngle, false);
                ctx.stroke();
            },

            addCurveCenter(ctx, fromPoint, toPoint, centerLabel, curveColor) {
                var returnObj = this.calcalateXY(fromPoint, toPoint);
                //console.log("startAngle,endAngle",returnObj.startAngle,returnObj.endAngle);
                var centerAngle = (returnObj.startAngle + returnObj.endAngle) / 2;
                //console.log("centerAngle",centerAngle);
                var centerX = Math.cos(centerAngle) * returnObj.radius;
                var centerY = Math.sin(centerAngle) * returnObj.radius;
                ctx.save();
                ctx.translate(returnObj.centerX, returnObj.centerY);
                var x2 = Math.cos(centerAngle) * returnObj.radius;
                var y2 = Math.sin(centerAngle) * returnObj.radius;
                var rotation2 = centerAngle + Math.PI / 2;
                this.addCurveCenterMarker(ctx, x2, y2, rotation2, centerLabel, curveColor);
                ctx.restore();
            },
            /**
             * 添加曲线箭头
             */
            addCurveArrow(ctx, fromPoint, toPoint, curveColor) {
                var returnObj = this.calcalateXY(fromPoint, toPoint);
                //console.log("startAngle,endAngle",returnObj.startAngle,returnObj.endAngle);
                var centerAngle = (returnObj.startAngle + returnObj.endAngle) / 2;
                //console.log("centerAngle",centerAngle);
                var centerX = Math.cos(centerAngle) * returnObj.radius;
                var centerY = Math.sin(centerAngle) * returnObj.radius;
                ctx.save();
                ctx.translate(returnObj.centerX, returnObj.centerY);
                var endx = Math.cos(returnObj.endAngle) * returnObj.radius;
                var endy = Math.sin(returnObj.endAngle) * returnObj.radius;
                var endRotation = returnObj.endAngle + Math.PI / 2;
                this.addCurveEndArrow(ctx, endx, endy, endRotation, curveColor);
                ctx.restore();
            },
            addCurveCenterMarker(ctx, x, y, rotation, centerLabel, curveColor) {
                ctx.translate(x, y);
                //ctx.rotate(rotation);
                // 设置字体
                ctx.font = "18px bold 黑体";
                // 设置颜色
                ctx.fillStyle = curveColor;
                // 设置水平对齐方式
                ctx.textAlign = "center";
                // 设置垂直对齐方式
                ctx.textBaseline = "middle";
                // 绘制文字（参数：要写的字，x坐标，y坐标）
                ctx.fillText(centerLabel, 0, 0);
            },

            addCurveEndArrow(ctx, x, y, rotation, curveColor) {

                var size = "7";
                ctx.save();
                ctx.translate(x, y);
                ctx.rotate(rotation);
                ctx.lineWidth = 0;
                ctx.strokeStyle = curveColor;
                ctx.fillStyle = curveColor;
                ctx.beginPath();
                //箭头
                ctx.moveTo(-size, -size);
                ctx.lineTo(size, 0);
                ctx.lineTo(-size, size);
                ctx.lineTo(-size / 4, 0);
                ctx.lineTo(-size, -size);

                ctx.closePath();
                ctx.stroke();
                ctx.fill();
                ctx.restore();
            },


            calcalateXY(fromPoint, toPoint) {
                var fromPixel = this.map.latLngToContainerPoint([fromPoint[0], fromPoint[1]]);
                var toPixel = this.map.latLngToContainerPoint([toPoint[0], toPoint[1]]);
                var startX = fromPixel.x;
                var startY = fromPixel.y;
                var endX = toPixel.x;
                var endY = toPixel.y;
                //两点之间的圆有多个，通过两点及半径便可以定出两个圆，根据需要选取其中一个圆
                var Leng = Math.sqrt(Math.pow(startX - endX, 2) + Math.pow(startY - endY, 2));
                var m = (startX + endX) / 2; // 横轴中点
                var n = (startY + endY) / 2; // 纵轴中点
                var factor = 1.5;
                var centerX = (startY - endY) * factor + m;
                var centerY = (endX - startX) * factor + n;
                var radius = Math.sqrt(Math.pow(Leng / 2, 2) + Math.pow(Leng * factor, 2));
                var startAngle = Math.atan2(startY - centerY, startX - centerX);
                var endAngle = Math.atan2(endY - centerY, endX - centerX);
                //console.log(centerX,centerY,radius,startAngle,endAngle);
                // 保证弧度不超过Math.PI
                if (startAngle * endAngle < 0) {
                    if (startAngle < 0) {
                        startAngle += Math.PI * 2;
                        endAngle += Math.PI * 2;
                    } else {
                        endAngle += Math.PI * 2;
                    }
                }
                var returnObj = {};
                returnObj.startX = startX;
                returnObj.startY = startY;
                returnObj.endX = endX;
                returnObj.endY = endY;

                returnObj.centerX = centerX;
                returnObj.centerY = centerY;
                returnObj.radius = radius;
                returnObj.startAngle = startAngle;
                returnObj.endAngle = endAngle;
                return returnObj;
            },


            addCanvasCurveMovePoint(ctx, fromPoint, toPoint, info, canvasDataObj, markerColor) {
                //console.log("addCanvasCurveMovePoint:",fromPoint,toPoint);
                var returnObj = this.calcalateXY(fromPoint, toPoint);
                //console.log(canvasDataObj.trailAngle);
                if (canvasDataObj.trailAngle == null) {
                    canvasDataObj.trailAngle = returnObj.startAngle;
                }
                // 匀速
                var angle = canvasDataObj.trailAngle + 4 / returnObj.radius;
                canvasDataObj.trailAngle = angle;
                var x2 = Math.cos(canvasDataObj.trailAngle) * returnObj.radius;
                var y2 = Math.sin(canvasDataObj.trailAngle) * returnObj.radius;
                var rotation = canvasDataObj.trailAngle + Math.PI / 2;

                ctx.save();
                ctx.translate(returnObj.centerX, returnObj.centerY);
                this.drawMovePoint(ctx, x2, y2, rotation, info.canvas, markerColor);
                ctx.restore();
                if ((returnObj.endAngle - canvasDataObj.trailAngle) * 180 / Math.PI < 0.5) {
                    canvasDataObj.trailAngle = returnObj.startAngle;
                }
            },
            drawMovePoint(ctx, x, y, rotation, canvas, markerColor) {
                //console.log("drawMovePoint:",x,y);
                var topLeft = this.map.containerPointToLayerPoint([0, 0]);
                L.DomUtil.setPosition(canvas, topLeft);
                ctx.save();
                ctx.translate(x, y);
                ctx.rotate(rotation);
                ctx.lineWidth = 0;
                ctx.strokeStyle = markerColor;
                ctx.fillStyle = markerColor;
                ctx.beginPath();
                ctx.arc(0, 0, 5, 0, Math.PI * 2, false);
                ctx.closePath();
                ctx.stroke();
                ctx.fill();
                ctx.restore();
            },

            /**
             * 清除迁徙图
             */
            clearMigration() {
                if (this.canvasLayer != null) {
                    this.canvasLayer.onRemove(this.map);
                    this.canvasLayer = null;
                }
                if (this.canvasMoveMarker != null) {
                    this.canvasMoveMarker.onRemove(this.map);
                    this.canvasMoveMarker = null;
                }
                if (this.cavasMoveMarkerInterval != null) {
                    window.clearInterval(this.cavasMoveMarkerInterval);
                    this.cavasMoveMarkerInterval = null;
                }

                this.outPolylineData = [];
                this.inPolylineData = [];

                this.map.off("zoomstart");
                this.map.off("zoomend");

                //清空地图
                //this.$refs.map.clearAllLayer();

                for (var i = 0; i < this.arrInfo.length; i++) {
                    this.removeLayer(this.arrInfo[i]);
                }
                this.arrInfo = [];
            }
        },
        mounted() {
            var date1 = new Date();
            var date2 = new Date("2021/04/23 23:59:59");
            if (date1.getTime() > date2.getTime()) {
                alert("地图在2021/04/23 23:59:59使用已到期！请联系管理人员");
            } else {
                this.loadMap();
            }
        }
    }

</script>

<style>
    .mapClass {
        position: absolute;
        top: 0;
        bottom: 0;
        right: 0;
        left: 0;
    }

    .peoplePhoto2-box {
        width: 0;
        height: 0;
        margin-left: 9.5px;
        margin-top: -2px;
        border-left: 10px solid transparent;
        border-right: 10px solid transparent;
        border-top: 10px solid blue;
    }
</style>
