package com.wumingtech.at.viewmodel.factory

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider


class ChildItemViewModelFactory(private val app: Application, private val id: Int, private val categoryCode: String, private val isVolume: Boolean,
                                private val templateId: Int) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = modelClass.getConstructor(
        Application::class.java, Int::class.java, String::class.java, Boolean::class.java, Int::class.java
    ).newInstance(app, id, categoryCode, isVolume, templateId)
}

class TemplateListViewModelFactory(private val app: Application, private val kindCode: String) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        modelClass.getConstructor(Application::class.java, String::class.java)
            .newInstance(app, kindCode)

}

class TemplateViewModelFactory(
    private val app: Application, private val categoryCode: String, private val isVolume: Boolean, private val tabValue: String, private val templateId: Int) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        modelClass.getConstructor(Application::class.java, String::class.java, Boolean::class.java, String::class.java, Int::class.java)
            .newInstance(app, categoryCode, isVolume, tabValue, templateId)
}

class ChildItemTemplateViewModelFactory(private val app: Application, private val categoryCode: String, private val isVolume: Boolean, private val templateId: Int) :
    ViewModelProvider
    .Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        modelClass.getConstructor(Application::class.java, String::class.java, Boolean::class.java, Int::class.java)
            .newInstance(app, categoryCode, isVolume, templateId)

}

class UserViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = modelClass.getConstructor(
        Application::class.java
    ).newInstance(app)
}

class SessionViewModelFactory(private val app: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = modelClass.getConstructor(
        Application::class.java
    ).newInstance(app)
}