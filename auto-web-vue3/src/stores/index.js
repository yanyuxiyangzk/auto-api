import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const device = ref('desktop')
  
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }
  
  function toggleDevice(deviceType) {
    device.value = deviceType
  }
  
  return {
    sidebarCollapsed,
    device,
    toggleSidebar,
    toggleDevice
  }
})

export const useUserStore = defineStore('user', () => {
  const userInfo = ref({
    id: 1,
    username: 'admin',
    name: '管理员',
    avatar: ''
  })
  const token = ref('')
  
  function setToken(newToken) {
    token.value = newToken
  }
  
  function setUserInfo(info) {
    userInfo.value = { ...userInfo.value, ...info }
  }
  
  function logout() {
    userInfo.value = {}
    token.value = ''
  }
  
  return {
    userInfo,
    token,
    setToken,
    setUserInfo,
    logout
  }
})

export const useDatasourceStore = defineStore('datasource', () => {
  const currentDatasource = ref(null)
  const datasourceList = ref([])
  
  function setCurrentDatasource(ds) {
    currentDatasource.value = ds
  }
  
  function setDatasourceList(list) {
    datasourceList.value = list
  }
  
  return {
    currentDatasource,
    datasourceList,
    setCurrentDatasource,
    setDatasourceList
  }
})

export const useTableStore = defineStore('table', () => {
  const selectedTables = ref([])
  const tableList = ref([])
  
  function setSelectedTables(tables) {
    selectedTables.value = tables
  }
  
  function setTableList(list) {
    tableList.value = list
  }
  
  return {
    selectedTables,
    tableList,
    setSelectedTables,
    setTableList
  }
})

export const useScriptStore = defineStore('script', () => {
  const currentScript = ref(null)
  const scriptList = ref([])
  
  function setCurrentScript(script) {
    currentScript.value = script
  }
  
  function setScriptList(list) {
    scriptList.value = list
  }
  
  return {
    currentScript,
    scriptList,
    setCurrentScript,
    setScriptList
  }
})
