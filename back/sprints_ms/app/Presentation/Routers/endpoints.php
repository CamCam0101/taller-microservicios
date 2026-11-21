<?php

use App\Presentation\Repositories\SprintsRepository;
use Slim\App;
use Slim\Routing\RouteCollectorProxy;

return function (App $app) {
    $app->group('/sprints', function (RouteCollectorProxy $group) {
        $group->get('', [SprintsRepository::class, 'get_all_sprints']);
        $group->get('/ultimo', [SprintsRepository::class, 'get_latest_sprint']);
        $group->get('/{id}', [SprintsRepository::class, 'get_sprint']);
        $group->post('', [SprintsRepository::class, 'create_sprint']);
        $group->put('/{id}', [SprintsRepository::class, 'update_sprint']);
        $group->delete('/{id}', [SprintsRepository::class, 'delete_sprint']);
    });
};