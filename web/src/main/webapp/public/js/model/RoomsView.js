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

var RoomsView = AbstractView.extend({

    initialize: function () {
        _.bindAll(this, 'render', 'bookInfo', 'renderRoomInfo', 'roomInfo', 'renderBookDialog', 'closeBookDialog');
        this.model.bind('change:schedule', this.render);
        this.model.bind('change:activeRoomInfo', this.renderRoomInfo);
        this.options.auth.bind('change', this.closeBookDialog);
        this.options.suggestions.bind('change', _.bind(this.renderMatches, {}, "#rooms", this.options.suggestions));
        this.setElement('#rooms');
        this.bookInfoView = new BookInfoView({
            model: this.model,
            auth: this.options.auth,
            manager: this.options.manager,
            attendees: this.options.attendees,
            dialog: this.options.dialog
        });
    },

    events: {
        'click .busy': 'bookInfo',
        'click .free': 'renderBookDialog',
        'click .schedule-row>h3': 'roomInfo',
        'click .book-label': 'bookInfo',
        'click .group-row h3': 'toggleRow'
    },

    closeBookDialog: function () {
        try {
            this.options.loginView.remove();
            this.bookDialog.remove();
        } catch (e) {
        }
    },

    renderBookDialog: function ($event) {
        this.closeBookDialog();

        var $target = $($event.target).parents().andSelf().filter('.slot.free');
        var principal = this.options.auth.get('principal');
        var showBookDialog = _.bind(function () {
            this.bookDialog = new BookDialogView({
                model: this.model,
                attendees: this.options.attendees,
                $target: $target,
                manager: this.options.manager,
                recurrence: this.options.recurrence,
                siteId: this.options.location.getCurrentSiteId(),
                dialog: this.options.dialog
            });
        }, this);

        if (!principal.loggedIn) {
            this.options.loginView.showDialog($target, {
                message: "Use Cinco credentials to login",
                success: showBookDialog
            });
        } else {
            showBookDialog();
        }
    },

    bookInfo: function ($event) {
        var $target = $($event.target);
        var eventId = $target.attr('event');

        if (this.model.get('activeInfo') != eventId) {
            this.model.set('activeInfo', $target.attr('event'));
            if (this.model.get('activeInfo') != 0) {
                this.bookInfoView.render();
            }
        }
    },

    roomInfo: function ($event) {
        var $target = $($event.target).parents('.schedule-row').first();
        this.model.set('activeRoomInfo', $target.attr('room'));
    },

    render: function () {
        var t = new Date().getTime();
        var $sch = this.$el;
        var schedule = this.model.get('schedule');
        var byBuilding = _.groupBy(schedule, function (s) {
            return s.room.attributes.building;
        });

        if (schedule.length === 0 && this.model.fromServer) {
            var height = $sch.innerHeight();
            $sch.html(preloader);
            $sch.find('.preloader').height(height);
        } else {
            $sch.html('');
        }

        var fakeRoomBldg = this.model.fakeRoom.building;
        var keys = _.sortBy(_.keys(byBuilding), function (bldg) {
            return bldg === fakeRoomBldg ? '' : bldg;
        });
        _.each(keys, function (bldg) {
            $.dustAppend('schedule-list', {building: bldg, schedule: _.sortBy(byBuilding[bldg], function (r) {
                return r.room.name;
            })}, $sch);
        });

        this.renderMatches("#rooms", this.options.suggestions);
    },

    renderRoomInfo: function () {
        if (this.model.get('activeRoomInfo') != 0) {
            var self = this;
            var room = this.model.getActiveRoomInfo();

            this.options.dialog.showModal(
                'room-info',
                room.email === this.model.fakeRoom.email ? {noroom: true} : room,
                {
                    onHide: function () {
                        self.model.set('activeRoomInfo', '');
                    }
                }
            );

            $.gaq('info', 'info-room', room.name);
        }
    }
});