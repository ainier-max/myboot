<template>
  <div>
    <div v-show="chooseFlag" align="center"
         style="background-color: white;float:left;margin-right: 20px;border:2px solid #FFFFF0;border-radius:10px;">
      <ul style="padding-inline-start:20px;" :class="chooseClass">
        <li v-for="(item,i) in chooseMapData" style="display:inline-block;list-style:none;padding-right: 20px">
          <div>
            <img style="cursor:pointer;" @click="changeMap(item.config)" width="80px" height="60px"
                 :src="item.img">
            <div style="font-size: 10px">{{item.title}}</div>
          </div>
        </li>
      </ul>
    </div>

    <div
      style="float: right;width: 35px;height: 35px;background-color: white;border:2px solid #FFFFF0;border-radius:10px;cursor: pointer"
      @click="showChooseWindow"
      align="center">
      <img style="padding-top: 6px" :src="defaultImg">
    </div>

  </div>
  <!--export default {-->
</template>

<script>
    module.exports = {
        name: "ChooseComponent",
        props: {
            mapRef: {type: String, request: true},//地图引用id
            chooseMapConfig: {type: Array, default: () => ([])},
        },
        data() {
            return {
                map: null,
                chooseFlag: false,
                chooseMapData: this.chooseMapConfig,
                defaultImg:'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABYAAAAWCAYAAADEtGw7AAAAGXRFWHRTb2Z0d2FyZQBBZG9iZSBJbWFnZVJlYWR5ccllPAAAA3ZpVFh0WE1MOmNvbS5hZG9iZS54bXAAAAAAADw/eHBhY2tldCBiZWdpbj0i77u/IiBpZD0iVzVNME1wQ2VoaUh6cmVTek5UY3prYzlkIj8+IDx4OnhtcG1ldGEgeG1sbnM6eD0iYWRvYmU6bnM6bWV0YS8iIHg6eG1wdGs9IkFkb2JlIFhNUCBDb3JlIDUuNi1jMTExIDc5LjE1ODMyNSwgMjAxNS8wOS8xMC0wMToxMDoyMCAgICAgICAgIj4gPHJkZjpSREYgeG1sbnM6cmRmPSJodHRwOi8vd3d3LnczLm9yZy8xOTk5LzAyLzIyLXJkZi1zeW50YXgtbnMjIj4gPHJkZjpEZXNjcmlwdGlvbiByZGY6YWJvdXQ9IiIgeG1sbnM6eG1wTU09Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC9tbS8iIHhtbG5zOnN0UmVmPSJodHRwOi8vbnMuYWRvYmUuY29tL3hhcC8xLjAvc1R5cGUvUmVzb3VyY2VSZWYjIiB4bWxuczp4bXA9Imh0dHA6Ly9ucy5hZG9iZS5jb20veGFwLzEuMC8iIHhtcE1NOk9yaWdpbmFsRG9jdW1lbnRJRD0ieG1wLmRpZDo1YmQ5ZjE2ZC0wYjUzLTQ0NDItOTk3YS1jMjRhZjEyNDcyYmUiIHhtcE1NOkRvY3VtZW50SUQ9InhtcC5kaWQ6QzQ2QTQxQTcxQTMxMTFFOUE4NkVDODg0M0E2MjU0OUIiIHhtcE1NOkluc3RhbmNlSUQ9InhtcC5paWQ6QzQ2QTQxQTYxQTMxMTFFOUE4NkVDODg0M0E2MjU0OUIiIHhtcDpDcmVhdG9yVG9vbD0iQWRvYmUgUGhvdG9zaG9wIENDIDIwMTUgKFdpbmRvd3MpIj4gPHhtcE1NOkRlcml2ZWRGcm9tIHN0UmVmOmluc3RhbmNlSUQ9InhtcC5paWQ6NWJkOWYxNmQtMGI1My00NDQyLTk5N2EtYzI0YWYxMjQ3MmJlIiBzdFJlZjpkb2N1bWVudElEPSJ4bXAuZGlkOjViZDlmMTZkLTBiNTMtNDQ0Mi05OTdhLWMyNGFmMTI0NzJiZSIvPiA8L3JkZjpEZXNjcmlwdGlvbj4gPC9yZGY6UkRGPiA8L3g6eG1wbWV0YT4gPD94cGFja2V0IGVuZD0iciI/Po4h5mAAAAOGSURBVHjajFVLaFNREJ25SZUgWK2NGz8LF7b+qEVBUNGNImpJo/jBL6gbF4pVBFFbQbSGgogLwZUgtChqXaS1uhBFXAQURF2ItVg3KkjTtDUooknueOZ94mubtj4Iee/emXPnzJyZyzTGc/FhegbneLswbSShWcRS42wIvyWmryz0SMrkXuOmaKaUP49cePMhPenBB24AwmkmnkbjPEIyBIhEXZVcXVoV/TMmsBsldRHzCsdR6BUskszyQL+tmLVG7EdhXgjHQ1ia754gL6SMNgejLwJfSvbNtsQpZp7jRZPH5sGzsWhrqWgdZt18HpZ71EdEPhuSlWfqZ37R/bBv1NVN7R5oD35zkIYIIvsZBGvuSO/L2fDmEOWvd75HVIYG8zacCnN+gwtO7cBao2kx6uDkVOkLZSyZAw5t4uNSsBlNj35f6Mg0iPDBMlOIGaYWY/gZGLXge2exFsBw64PXk60DU6dPtZ+QlBk5G7oTX5Df39VDi9hKWmkpsORDVUx2NewnQw3H1HacimYGs2ZeeHp5oR74jqGejhPfNcYqL/h2Q5lQblq53eUVC+mRvC6zcKHkAVhDoLuNEG8LLPcImSfKwl8oL6fF+JuLfO8VomuIqA3ez6Hv21j/VTJoaD+MSKp9ClZoU1O8ojdoZLjwC5bVkFQC4NC2TMFyI+xXIbpI6XRIbRiymsmqOpbjTfXR3lE2VnVMjgRZ5L5yRfWfYScC8E4U7Ae8j6jmYbfMrSFXGh/AWk75CghqVY1GpZGlAn9PhrKhY6jHUWj4ZmN95XKk6pzXA7+hHO5zKcuCkX2veoT81sF0vyrmX+FpIaJ7enlfRdYpMA5wGZmXbv24D8Wjbi+Mu83J/n78DgfBm2IVKe2+slC+JSCptmBH+gcI2xrv4G5ELO3efsSRD9OVix39yebOdO0w+tB14OtbczJdV0LDO9zI6ZEZ/B5KulOq+ERQjJgtcDboo82i80AlNpQ1CcuhgWEDrDOzVYunWGiQW8alwYnhB9O5pnjlKIWQMQ0kZov6aIqKM0TZib3hsUnoPgcmVco/0Ygs8afURI+jpBy99obQq7pqWVkcQu6LjStVHSg6PpXaRKBOnougOjZt3B/4wwa9zmS0ePuIQX8Xun3sF08MRzHl1qNZtvp2Oughgm1BlmNdTadgfeI/r6YruJpaxr2axrtM/Xb1WEx4mf4VYAAHMsDQlBRAoQAAAABJRU5ErkJggg==',
                chooseClass:''
            }
        },
        computed: {},
        watch: {},
        components: {},
        methods: {
            initCoponent() {
                this.map = this.$parent.$refs[this.mapRef];
                console.log("地图选择组件地图对象：this.map:",this.map);
                if (typeof (this.map) === "undefined") {
                    window.setTimeout(this.initCoponent, 100);
                }else{
                    if(this.map.type=="vue"){
                        this.chooseClass="ulClass";
                    }
                }
            },
            changeMap(mapConfig) {
                console.log("地图配置：", mapConfig);
                this.map.loadMapByParam(mapConfig);
            },
            showChooseWindow() {
                if (this.chooseFlag==true) {
                    this.chooseFlag=false;
                }else{
                    this.chooseFlag = true;
                }

            },
        },
        beforeCreate() {
        },
        created() {
        },
        beforeMount() {
        },
        mounted() {
            this.initCoponent();
        },
        beforeUpdate() {
        },
        updated() {
        },
        beforeDestroy() {
        },
        destroyed() {
        }
    }
</script>

<style scoped>
  .ulClass{
    padding-top: 15px;
    padding-bottom: 5px;
  }
</style>
