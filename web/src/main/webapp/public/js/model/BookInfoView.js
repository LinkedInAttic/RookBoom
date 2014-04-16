/*
 * (c) Copyright 2014 LinkedIn Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var BookInfoView = Backbone.View.extend({

    events: {
        'click .edit .cancel': 'handleCancel'
    },

    initialize: function (params) {
        _.bindAll(this, 'render', 'resetActive', 'showDialog', 'handleCancel');
        this.auth = params.auth;
        this.setElement('body');
    },

    render: function() {
        this.active = this.model.get('activeInfo');
        this.$labelOuter = $('.busy-label[event="' + this.active + '"]').parent();
        this.showDialog();
        this.toClose = {
            $labelOuter: this.$labelOuter,
            active: this.active
        }
    },

    showDialog: function () {
        try {
            var user = this.auth.get('principal').user;
            var event = this.model.get('events')[this.active];
            var isOwner = user
                && event.appointment
                && event.appointment.owner
                && event.appointment.owner.account === user.account;
        } catch (e) {
            return;
        }

        this.$labelOuter.addClass('active');

        this.options.dialog.showPopover(
            'book_info',
            {
                event: event,
                user: user,
                isOwner: isOwner
            },
            this.$labelOuter,
            {
                onHide: this.resetActive
            }
        );
    },

    handleCancel: function () {
        var self = this;
        $.confirmMsg('Really want to cancel this meeting?', function () {
            self.options.dialog.blackout();
            $('.cancel-form').ajaxSubmit($.withErrorHandling({
                success: function (data) {
                    self.options.dialog.hide();
                    if (data && data.success === true) {
                        $.successMsg('Meeting has been cancelled!');
                        self.options.manager.onDateChange();
                        self.options.attendees.onDateChange();
                        $.gaq('cancel', 'cancel-succeed');
                    } else {
                        $.errorMsg('Something went wrong.<br/>Meeting has not been cancelled');
                        $.gaq('cancel', 'cancel-failed');
                    }
                },
                error: function() {
                    $.gaq('cancel', 'cancel-failed');
                    self.options.dialog.hide();
                },
                errorMsg: 'Something went wrong.<br/>Meeting has not been cancelled'
            }));
        });
    },

    resetActive: function () {
        var toClose = this.toClose;
        if (toClose.active === this.model.get('activeInfo')) {
            this.model.set('activeInfo', 0);
        }
        try {
            toClose.$labelOuter.removeClass('active');
        } catch (e) {
        }
    }
});