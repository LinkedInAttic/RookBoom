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

var AbstractView = Backbone.View.extend({

    toggleRow: function ($event) {
        var $target = $($event.target).parents().andSelf().filter('h3')
        var $list = $('#' + $target.attr('target'))
        $list.slideToggle(200, function () {
            $target.find('img').toggle();
        })
    },

    renderMatches: function (rootElement, suggestions) {
        var slots = suggestions.get('slots');

        var map = _.reduce(slots, function (map, slot) {
            map[slot.from] = slot.match;
            return map;
        }, {});

        $(rootElement + ' .slot.free .match').each(function () {
            var $div = $(this);
            var from = $div.attr('from');
            $div.removeClass('none');
            $div.removeClass('some');
            $div.removeClass('all');
            $div.addClass(map[from]);
        });
    }
});