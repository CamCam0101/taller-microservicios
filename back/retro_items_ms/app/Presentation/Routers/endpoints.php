<?php

use App\Presentation\Repositories\RetroItemsRepository;
use Slim\App;
use Slim\Routing\RouteCollectorProxy;

return function (App $app) {
    $app->group('/items', function (RouteCollectorProxy $group) {
        $group->get('', [RetroItemsRepository::class, 'get_all_items']);
        $group->get('/sprint/{sprint_id}', [RetroItemsRepository::class, 'get_items_by_sprint']);
        $group->get('/{id}', [RetroItemsRepository::class, 'get_item']);
        $group->post('', [RetroItemsRepository::class, 'create_item']);
        $group->put('/{id}', [RetroItemsRepository::class, 'update_item']);
        $group->delete('/{id}', [RetroItemsRepository::class, 'delete_item']);
    });
};