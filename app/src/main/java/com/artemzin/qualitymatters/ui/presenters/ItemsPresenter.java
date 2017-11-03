package com.artemzin.qualitymatters.ui.presenters;

import android.support.annotation.NonNull;

import com.artemzin.qualitymatters.models.AnalyticsModel;
import com.artemzin.qualitymatters.models.ItemsModel;
import com.artemzin.qualitymatters.other.FinishAsyncJobAction;
import com.artemzin.qualitymatters.performance.AsyncJob;
import com.artemzin.qualitymatters.performance.AsyncJobsObserver;
import com.artemzin.qualitymatters.ui.views.ItemsView;

import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;

public class ItemsPresenter extends Presenter<ItemsView> {

    @NonNull
    private final ItemsPresenterConfiguration presenterConfiguration;

    @NonNull
    private final ItemsModel itemsModel;

    @NonNull
    private final AsyncJobsObserver asyncJobsObserver;

    @NonNull
    private final AnalyticsModel analyticsModel;

    public ItemsPresenter(@NonNull ItemsPresenterConfiguration presenterConfiguration,
                          @NonNull ItemsModel itemsModel,
                          @NonNull AsyncJobsObserver asyncJobsObserver,
                          @NonNull AnalyticsModel analyticsModel) {
        this.presenterConfiguration = presenterConfiguration;
        this.itemsModel = itemsModel;
        this.asyncJobsObserver = asyncJobsObserver;
        this.analyticsModel = analyticsModel;
    }

    public void reloadData() {
        {
            // Tip: in Kotlin you can use ? to operate with nullable values.
            final ItemsView view = view();

            if (view != null) {
                view.showLoadingUi();
            }
        }

        final AsyncJob asyncJob = asyncJobsObserver.asyncJobStarted("Reload data in ItemsPresenter");

        final Disposable disposable = itemsModel
                .getItems()
                .subscribeOn(presenterConfiguration.ioScheduler())
                .subscribe(
                        items -> {
                            // Tip: in Kotlin you can use ? to operate with nullable values.
                            final ItemsView view = view();

                            if (view != null) {
                                view.showContentUi(items);
                            }

                            asyncJobsObserver.asyncJobFinished(asyncJob);
                        },
                        error -> {
                            analyticsModel.sendError("ItemsPresenter.reloadData failed", error);

                            // Tip: in Kotlin you can use ? to operate with nullable values.
                            final ItemsView view = view();

                            if (view != null) {
                                view.showErrorUi(error);
                            }

                            asyncJobsObserver.asyncJobFinished(asyncJob);
                        }
                );

        // Prevent memory leak.
        Disposable finishAsyncJobDisposable
                = Disposables.fromAction(new FinishAsyncJobAction(asyncJobsObserver, asyncJob));
        unsubscribeOnUnbindView(disposable, finishAsyncJobDisposable);
    }
}
