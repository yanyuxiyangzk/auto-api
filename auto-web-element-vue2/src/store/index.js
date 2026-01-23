import Vue from 'vue'
import Vuex from 'vuex'
import app from './modules/app'
import user from './modules/user'
import permission from './modules/permission'
import datasource from './modules/datasource'
import table from './modules/table'
import script from './modules/script'

Vue.use(Vuex)

export default new Vuex.Store({
  modules: {
    app,
    user,
    permission,
    datasource,
    table,
    script
  },
  strict: process.env.NODE_ENV !== 'production'
})
