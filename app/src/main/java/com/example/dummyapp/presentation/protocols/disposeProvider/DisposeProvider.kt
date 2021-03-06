package com.example.dummyapp.presentation.protocols.disposeProvider

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.example.dummyapp.presentation.protocols.errorProtocol.ErrorProtocol
import com.example.dummyapp.presentation.protocols.ui.Loadable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

interface DisposablePresenter<View> : Presenter<View>, DisposeProvider {
    override var attachedUnsafeView: View?
    
    override fun attach(view: View, lifecycle: Lifecycle) {
        super<Presenter>.attach(view, lifecycle)
        super<DisposeProvider>.attach(lifecycle)
    }
    
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            destroy()
        }
    }
    
    override fun destroy() {
        super<Presenter>.destroy()
        super<DisposeProvider>.destroy()
    }
}

interface DisposeProvider : LifecycleEventObserver {
    val disposeBag: CompositeDisposable
    
    fun attach(lifecycle: Lifecycle) {
        lifecycle.addObserver(this)
    }
    
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            destroy()
        }
    }
    
    fun destroy() {
        disposeBag.dispose()
    }
    
    fun <T : Any, ViewType> Single<T>.subscribeOnSuccess(view: ViewType, success: (T) -> Unit): Disposable where ViewType: Loadable, ViewType: ErrorProtocol =
        doOnEvent { _, _ -> view.onHideLoading() }
            .subscribe({ success(it) }, { view.onReceiveError(it) })
}

operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
    add(disposable)
}

interface Presenter<View> : LifecycleEventObserver {
    var attachedUnsafeView: View?
    
    @Suppress("UNCHECKED_CAST")
    val view: View
        get() = attachedUnsafeView as View
    
    fun attach(view: View, lifecycle: Lifecycle) {
        this.attachedUnsafeView = view
        lifecycle.addObserver(this)
    }
    
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            destroy()
        }
    }

    fun destroy() {
        attachedUnsafeView = null
    }
}