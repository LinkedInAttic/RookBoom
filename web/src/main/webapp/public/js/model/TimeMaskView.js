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

var TimeMaskView = Backbone.View.extend({

    lines: new Array(),

    initialize: function () {
        _.bindAll(this, 'render', 'singleLine', 'fullLine', 'removeLines', 'hourTimeLine', 'currentTimeLine', 'redrawLines', 'redrawTimeRow', 'filterRoundHour', 'adjustBoardWidth');
        this.model.bind('change', this.render);
        this.options.filterValues.bind('change:location', this.render);
        this.options.filterValues.bind('change:timeframe', this.render);
    },

    singleLine: function (elem, clazz, css) {
        var $div = $('<div class="' + clazz + '"></div>');
        var $elem = $(elem);
        for (var name in css) {
            $div.css(name, css[name]);
        }
        $elem.append($div);
        this.lines.push($div);
    },

    fullLine: function (clazz, left) {
        var offset = {
            'margin-left': left + 'px'
        };

        this.singleLine('#board-header', clazz, $.extend({
            'margin-top': '-5px',
            'height': '5px'
        }, offset));

        this.singleLine('#schedule', clazz, $.extend({
            'margin-top': '-15035px',
            'height': '15000px'
        }, offset));
    },

    hourTimeLine: function (i, e) {
        var $e = $(e).parent();
        var left = $e.position().left - 1 + $e.width() / 2;
        this.fullLine('timeline', left);
    },

    currentTimeLine: function (filtered) {
        var hour = 1000 * 60 * 60;
        var halfHour = hour / 2;
        var now = this.options.location.getCurrentTimeForLocation();
        var frame = _.find(filtered.frames, function (f) {
            return f - halfHour <= now && f + halfHour > now;
        });
        if (frame) {
            var pos = (now - (frame - halfHour)) / hour;
            var $el = $('.time[time="' + frame + '"]').parent();
            var left = $el.position().left + $el.width() * pos;
            this.fullLine('now line', left);
            this.singleLine('#schedule', 'now pointer up', {
                'margin-left': left + 'px'
            });
            this.singleLine('#board-header', 'now pointer down', {
                'margin-left': left + 'px'
            });
        }
    },

    removeLines: function () {
        _.each(this.lines, function ($line) {
            $line.remove();
        });
        this.lines = new Array();
    },

    redrawLines: function (filtered) {
        this.removeLines();
        $('.time-mask-row.top .time').each(this.hourTimeLine);
        this.currentTimeLine(filtered)
    },

    redrawTimeRow: function (filtered) {
        $.dustReplace('time-mask', filtered, '.time-mask-row', function () {
            $.updateTime({
                selector: '.time-mask-row .time'
            });
        });
    },

    filterRoundHour: function () {
        return {
            frames: _.filter(this.model.get('timeMask').frames, function (f) {
                return new Date(f).getMinutes() === 0;
            })
        };
    },

    adjustBoardWidth: function () {
        var slotsNum = this.model.get('timeMask').frames.length;
        var width = Math.max(800, slotsNum * 41 + 205);
        $('.width-aware').width(width);
        $('.qqq').width(width + 100);
    },

    render: function () {
        this.adjustBoardWidth();
        var filtered = this.filterRoundHour();
        this.redrawTimeRow(filtered);
        this.redrawLines(filtered);
        setTimeout(this.render, 10000);
    }
});